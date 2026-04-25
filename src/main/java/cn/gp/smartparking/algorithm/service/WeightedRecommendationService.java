package cn.gp.smartparking.algorithm.service;

import cn.gp.smartparking.algorithm.config.AlgorithmConfig;
import cn.gp.smartparking.algorithm.entity.RecommendRequest;
import cn.gp.smartparking.algorithm.entity.RecommendResult;
import cn.gp.smartparking.algorithm.mapper.AlgorithmDataMapper;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import cn.gp.smartparking.model.entity.UserPreference;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeightedRecommendationService {

    @Resource
    private AlgorithmDataMapper algorithmDataMapper;
    private final AlgorithmConfig algorithmConfig;

    public List<RecommendResult> recommend(RecommendRequest request) {
        List<ParkingLot> allParkingLots = algorithmDataMapper.selectActiveParkingLots();
        if (allParkingLots.isEmpty()) {
            return new ArrayList<>();
        }

        UserPreference userPreference = null;
        if (request.getUserId() != null) {
            userPreference = algorithmDataMapper.selectUserPreferenceByUserId(request.getUserId());
        }

        List<RecommendResult> results = new ArrayList<>();
        for (ParkingLot parkingLot : allParkingLots) {
            RecommendResult result = calculateRecommendation(parkingLot, request, userPreference);
            results.add(result);
        }

        return results.stream()
                .sorted(Comparator.comparingDouble(RecommendResult::getScore).reversed())
                .limit(request.getRecommendCount())
                .collect(Collectors.toList());
    }

    private RecommendResult calculateRecommendation(ParkingLot parkingLot, RecommendRequest request, UserPreference userPreference) {
        double distanceScore = calculateDistanceScore(parkingLot, request);
        double priceScore = calculatePriceScore(parkingLot);
        double availabilityScore = calculateEnhancedAvailabilityScore(parkingLot);
        double preferenceScore = calculatePreferenceScore(parkingLot, userPreference);
        double timeOfDayScore = calculateTimeOfDayScore(parkingLot);
        double historicalScore = calculateHistoricalPerformanceScore(parkingLot);

        // 动态权重调整
        double distanceWeight = algorithmConfig.getDistanceWeight();
        double priceWeight = algorithmConfig.getRatingWeight();
        double availabilityWeight = algorithmConfig.getFreeSpaceWeight();
        double preferenceWeight = algorithmConfig.getPreferenceWeight();
        double historicalWeight = 0.15; // 历史表现权重

        // 根据时间调整权重
        int hour = java.time.LocalDateTime.now().getHour();
        if (hour >= 7 && hour <= 9 || hour >= 17 && hour <= 19) {
            // 高峰时段，更重视可用性和历史表现
            availabilityWeight *= 1.5;
            historicalWeight *= 1.3;
            distanceWeight *= 0.8;
        } else if (hour >= 22 || hour <= 6) {
            // 夜间时段，更重视距离和安全性
            distanceWeight *= 1.3;
            availabilityWeight *= 0.7;
            historicalWeight *= 0.9;
        }

        double totalScore = distanceScore * distanceWeight +
                priceScore * priceWeight +
                availabilityScore * availabilityWeight +
                preferenceScore * preferenceWeight +
                timeOfDayScore * 0.1 +
                historicalScore * historicalWeight;

        RecommendResult result = new RecommendResult();
        result.setParkingLotId(parkingLot.getId());
        result.setParkingLotName(parkingLot.getName() != null ? parkingLot.getName() : "未知停车场");
        result.setAddress(parkingLot.getAddress() != null ? parkingLot.getAddress() : "地址未知");
        result.setDistance(calculateDistance(parkingLot, request));
        result.setRate(parkingLot.getRate() != null ? parkingLot.getRate() : BigDecimal.ZERO);
        result.setFreeSpace(parkingLot.getFreeSpace() != null ? parkingLot.getFreeSpace() : 0);
        result.setTotalSpace(parkingLot.getTotalSpace() != null ? parkingLot.getTotalSpace() : 100);
        result.setScore(Math.round(totalScore * 100.0) / 100.0);
        result.setTip(generateAdvancedTip(totalScore, parkingLot, request));

        return result;
    }

    private double calculateDistanceScore(ParkingLot parkingLot, RecommendRequest request) {
        BigDecimal distance = calculateDistance(parkingLot, request);
        double maxDistance = algorithmConfig.getRecommendRadius();
        double score = 1.0 - (distance.doubleValue() / maxDistance);
        return Math.max(0, Math.min(1, score));
    }

    private BigDecimal calculateDistance(ParkingLot parkingLot, RecommendRequest request) {
        if (parkingLot.getLongitude() == null || parkingLot.getLatitude() == null) {
            return BigDecimal.valueOf(algorithmConfig.getRecommendRadius());
        }

        double lat1 = Math.toRadians(request.getLatitude());
        double lon1 = Math.toRadians(request.getLongitude());
        double lat2 = Math.toRadians(parkingLot.getLatitude().doubleValue());
        double lon2 = Math.toRadians(parkingLot.getLongitude().doubleValue());

        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distance = 6371 * c * 1000;

        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }

    private double calculatePriceScore(ParkingLot parkingLot) {
        if (parkingLot.getRate() == null) {
            return 0.5;
        }
        double rate = parkingLot.getRate().doubleValue();
        if (rate <= 5) {
            return 1.0;
        } else if (rate <= 10) {
            return 0.8;
        } else if (rate <= 20) {
            return 0.5;
        } else {
            return 0.2;
        }
    }

    private double calculateEnhancedAvailabilityScore(ParkingLot parkingLot) {
        if (parkingLot.getTotalSpace() == null || parkingLot.getTotalSpace() == 0) {
            return 0.0;
        }
        
        double freeSpaceRatio = (double) parkingLot.getFreeSpace() / parkingLot.getTotalSpace();
        
        // 获取历史数据来预测未来可用性
        double historicalTrend = getHistoricalAvailabilityTrend(parkingLot.getId());
        
        // 考虑实时可用性和趋势
        int hour = java.time.LocalDateTime.now().getHour();
        double timeMultiplier = 1.0;
        
        // 高峰时段可用性更重要
        if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
            timeMultiplier = 1.2;
        }
        
        // 可用空间评分曲线，结合历史趋势
        double baseScore = 0.0;
        if (freeSpaceRatio >= 0.5) {
            baseScore = 1.0;
        } else if (freeSpaceRatio >= 0.3) {
            baseScore = 0.8;
        } else if (freeSpaceRatio >= 0.1) {
            baseScore = 0.5;
        } else {
            baseScore = 0.1;
        }
        
        // 结合历史趋势调整评分
        double adjustedScore = baseScore * (1.0 + historicalTrend * 0.3) * timeMultiplier;
        return Math.max(0.0, Math.min(1.0, adjustedScore));
    }

    private double calculatePreferenceScore(ParkingLot parkingLot, UserPreference userPreference) {
        if (userPreference == null) {
            return 0.5;
        }

        double score = 0.5;

        if (userPreference.getPreferDistance() != null) {
            BigDecimal distance = calculateDistance(parkingLot, new RecommendRequest());
            if (distance.doubleValue() <= userPreference.getPreferDistance()) {
                score += 0.25;
            }
        }

        if (userPreference.getPreferPrice() != null) {
            if (parkingLot.getRate() != null) {
                double rate = parkingLot.getRate().doubleValue();
                if (userPreference.getPreferPrice() == 0 && rate <= 10) {
                    score += 0.25;
                } else if (userPreference.getPreferPrice() == 1 && calculatePriceScore(parkingLot) > 0.7) {
                    score += 0.25;
                }
            }
        }

        return Math.min(1.0, score);
    }

    private String generateAdvancedTip(double score, ParkingLot parkingLot, RecommendRequest request) {
        int hour = java.time.LocalDateTime.now().getHour();
        double distance = calculateDistance(parkingLot, request).doubleValue();
        
        if (score >= 0.8) {
            return String.format("强烈推荐：距离%.0fm，费率%s，空位充足，适合当前时段", 
                distance, parkingLot.getRate() != null ? "¥" + parkingLot.getRate() : "未知");
        } else if (score >= 0.6) {
            if (distance > 2000) {
                return "推荐：综合表现良好，但距离较远，建议驾车前往";
            } else {
                return "推荐：综合表现良好，步行可达";
            }
        } else if (score >= 0.4) {
            if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
                return "一般：高峰时段可能紧张，建议提前预约";
            } else {
                return "一般：可作为备选方案";
            }
        } else {
            if (parkingLot.getFreeSpace() != null && parkingLot.getFreeSpace() < 5) {
                return "不推荐：车位即将满员，建议选择其他停车场";
            } else {
                return "不推荐：距离较远或空位不足";
            }
        }
    }

    private double calculateTimeOfDayScore(ParkingLot parkingLot) {
        int hour = java.time.LocalDateTime.now().getHour();
        int dayOfWeek = java.time.LocalDateTime.now().getDayOfWeek().getValue();
        
        // 基于历史数据的时段评分
        double baseScore = 0.5;
        
        // 工作日模式
        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            if (hour >= 8 && hour <= 18) {
                baseScore = 0.8; // 工作时间评分较高
            } else if (hour >= 19 && hour <= 21) {
                baseScore = 0.6; // 晚间用餐时间
            } else {
                baseScore = 0.4; // 非高峰时间
            }
        } else {
            // 周末模式
            if (hour >= 10 && hour <= 22) {
                baseScore = 0.7; // 周末活跃时间
            } else {
                baseScore = 0.3; // 周末深夜
            }
        }
        
        return baseScore;
    }

    /**
     * 计算历史表现评分
     */
    private double calculateHistoricalPerformanceScore(ParkingLot parkingLot) {
        try {
            // 获取过去7天同一时段的历史数据
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusDays(7);
            
            List<ParkingUsageHistory> histories = algorithmDataMapper.selectHistoryByTimeRange(
                parkingLot.getId(), weekAgo, now);
            
            if (histories.isEmpty()) {
                return 0.5; // 默认评分
            }
            
            // 计算平均可用性和稳定性
            double avgOccupancy = histories.stream()
                .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
                .average()
                .orElse(50.0);
            
            // 可用性评分（占用率越低越好）
            double availabilityScore = Math.max(0, (100 - avgOccupancy) / 100);
            
            // 稳定性评分（方差越小越好）
            double variance = calculateVariance(histories);
            double stabilityScore = Math.max(0, 1.0 - variance / 1000.0);
            
            // 综合历史表现评分
            return (availabilityScore * 0.7 + stabilityScore * 0.3);
            
        } catch (Exception e) {
            log.error("计算历史表现评分失败: {}", e.getMessage());
            return 0.5;
        }
    }

    /**
     * 获取历史可用性趋势
     */
    private double getHistoricalAvailabilityTrend(Long parkingLotId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysAgo = now.minusDays(3);
            
            List<ParkingUsageHistory> recentHistories = algorithmDataMapper.selectHistoryByTimeRange(
                parkingLotId, threeDaysAgo, now);
            
            if (recentHistories.size() < 4) {
                return 0.0; // 数据不足，无趋势
            }
            
            // 计算趋势：最近的数据 vs 较早的数据
            int midPoint = recentHistories.size() / 2;
            
            double recentAvg = recentHistories.subList(0, midPoint).stream()
                .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
                .average()
                .orElse(50.0);
            
            double earlierAvg = recentHistories.subList(midPoint, recentHistories.size()).stream()
                .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
                .average()
                .orElse(50.0);
            
            // 趋势：正值表示占用率上升（可用性下降），负值表示可用性上升
            return (earlierAvg - recentAvg) / 50.0; // 归一化到[-1,1]
            
        } catch (Exception e) {
            log.error("获取历史可用性趋势失败: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * 计算方差
     */
    private double calculateVariance(List<ParkingUsageHistory> histories) {
        if (histories.isEmpty()) return 0.0;
        
        double mean = histories.stream()
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
        
        double sumSquaredDiff = histories.stream()
            .mapToDouble(h -> {
                double value = h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0;
                return Math.pow(value - mean, 2);
            })
            .sum();
        
        return sumSquaredDiff / histories.size();
    }
}