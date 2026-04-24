package cn.gp.smartparking.algorithm.service;

import cn.gp.smartparking.algorithm.config.AlgorithmConfig;
import cn.gp.smartparking.algorithm.entity.PredictionResult;
import cn.gp.smartparking.algorithm.mapper.AlgorithmDataMapper;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSeriesPredictorService {

    @Resource
    private AlgorithmDataMapper algorithmDataMapper;

    private final AlgorithmConfig algorithmConfig;

    private static final String[] FEATURE_NAMES = {"hour", "dayOfWeek", "isWeekend", "historicalRate"};

    public List<PredictionResult> predict(Long parkingLotId, Integer hoursAhead) {
        List<PredictionResult> results = new ArrayList<>();

        if (parkingLotId == null) {
            List<ParkingUsageHistory> histories = getRecentHistories(parkingLotId, 168);
            results.addAll(predictForParkingLots(histories, hoursAhead));
        } else {
            results.addAll(predictForSingleParkingLot(parkingLotId, hoursAhead));
        }

        return results;
    }

    public PredictionResult predictNextHour(Long parkingLotId) {
        List<PredictionResult> results = predictForSingleParkingLot(parkingLotId, 1);
        return results.isEmpty() ? null : results.get(0);
    }

    private List<PredictionResult> predictForParkingLots(List<ParkingUsageHistory> histories, Integer hoursAhead) {
        List<Long> parkingLotIds = histories.stream()
                .map(ParkingUsageHistory::getParkingLotId)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());

        List<PredictionResult> allResults = new ArrayList<>();
        for (Long parkingLotId : parkingLotIds) {
            List<PredictionResult> results = predictForSingleParkingLot(parkingLotId, hoursAhead);
            allResults.addAll(results);
        }

        return allResults;
    }

    private List<PredictionResult> predictForSingleParkingLot(Long parkingLotId, Integer hoursAhead) {
        List<PredictionResult> results = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusHours(algorithmConfig.getPredictionHistoryHours());

        List<ParkingUsageHistory> histories = algorithmDataMapper
                .selectHistoryByParkingLotAndTimeRange(parkingLotId, startTime);

        if (histories.size() < 24) {
            return generateSimplePrediction(parkingLotId, hoursAhead);
        }

        // Simplified prediction without external ML libraries
        return generateSimplePrediction(parkingLotId, hoursAhead);
    }

    // Removed external ML dependencies for simpler implementation

    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return LocalDateTime.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // Removed external ML dependencies

    // Removed external ML dependencies

    // Removed external ML dependencies

    private List<PredictionResult> generateSimplePrediction(Long parkingLotId, Integer hoursAhead) {
        List<PredictionResult> results = new ArrayList<>();

        // 获取历史数据进行更准确的预测
        List<ParkingUsageHistory> historicalData = getEnhancedHistoricalData(parkingLotId);
        
        LocalDateTime now = LocalDateTime.now();
        for (int i = 1; i <= hoursAhead; i++) {
            LocalDateTime predictTime = now.plusHours(i);
            
            // 使用多种方法计算预测值
            Double hourlyRate = calculateAdvancedPrediction(parkingLotId, predictTime, historicalData);
            
            PredictionResult result = new PredictionResult();
            result.setPredictTime(predictTime);
            result.setPredictedOccupancyRate(BigDecimal.valueOf(hourlyRate).setScale(2, RoundingMode.HALF_UP));
            result.setConfidence(calculateAdvancedConfidence(parkingLotId, predictTime, historicalData, i));
            result.setModelName("Enhanced-TimeSeries-v2");
            result.setTip(generateAdvancedTip(hourlyRate / 100.0, predictTime));

            results.add(result);
        }

        return results;
    }

    private List<ParkingUsageHistory> getRecentHistories(Long parkingLotId, int limit) {
        return algorithmDataMapper.selectRecentHistory(parkingLotId, limit);
    }

    /**
     * 获取增强的历史数据
     */
    private List<ParkingUsageHistory> getEnhancedHistoricalData(Long parkingLotId) {
        try {
            // 获取过去14天的数据
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(14);
            
            List<ParkingUsageHistory> histories = algorithmDataMapper.selectHistoryByTimeRange(
                parkingLotId, startTime, endTime);
            
            // 数据清洗：去除异常值
            return cleanHistoricalData(histories);
            
        } catch (Exception e) {
            log.error("获取历史数据失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 数据清洗：去除异常值
     */
    private List<ParkingUsageHistory> cleanHistoricalData(List<ParkingUsageHistory> histories) {
        if (histories.isEmpty()) return histories;
        
        // 计算平均值和标准差
        double mean = histories.stream()
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
        
        double stdDev = Math.sqrt(histories.stream()
            .mapToDouble(h -> {
                double value = h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0;
                return Math.pow(value - mean, 2);
            })
            .average()
            .orElse(0.0));
        
        // 去除超出3个标准差的异常值
        return histories.stream()
            .filter(h -> {
                double value = h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0;
                return Math.abs(value - mean) <= 3 * stdDev;
            })
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 高级预测算法
     */
    private Double calculateAdvancedPrediction(Long parkingLotId, LocalDateTime predictTime, 
                                            List<ParkingUsageHistory> historicalData) {
        if (historicalData.isEmpty()) {
            return calculateEnhancedRate(50.0, predictTime);
        }
        
        // 方法1：基于历史同一时段的平均值
        double historicalAvg = calculateHistoricalHourlyAverage(historicalData, predictTime.getHour());
        
        // 方法2：基于趋势预测
        double trendPrediction = calculateTrendBasedPrediction(historicalData, predictTime);
        
        // 方法3：基于周期性模式
        double seasonalPrediction = calculateSeasonalPrediction(historicalData, predictTime);
        
        // 加权融合多种预测方法
        double finalPrediction = historicalAvg * 0.4 + trendPrediction * 0.3 + seasonalPrediction * 0.3;
        
        // 应用时间调整因子
        return calculateEnhancedRate(finalPrediction, predictTime);
    }

    /**
     * 计算历史同一时段平均值
     */
    private double calculateHistoricalHourlyAverage(List<ParkingUsageHistory> histories, int targetHour) {
        return histories.stream()
            .filter(h -> h.getHour() != null && h.getHour().equals(targetHour))
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
    }

    /**
     * 基于趋势的预测
     */
    private double calculateTrendBasedPrediction(List<ParkingUsageHistory> histories, LocalDateTime predictTime) {
        if (histories.size() < 4) return 50.0;
        
        // 简单的线性趋势预测
        int recentCount = Math.min(7, histories.size() / 2);
        int olderCount = Math.min(7, histories.size() / 2);
        
        double recentAvg = histories.stream()
            .skip(histories.size() - recentCount)
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
        
        double olderAvg = histories.stream()
            .limit(olderCount)
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
        
        // 计算趋势并外推
        double trend = recentAvg - olderAvg;
        return recentAvg + trend * 0.5; // 保守的趋势外推
    }

    /**
     * 基于周期性的预测
     */
    private double calculateSeasonalPrediction(List<ParkingUsageHistory> histories, LocalDateTime predictTime) {
        int dayOfWeek = predictTime.getDayOfWeek().getValue();
        int hour = predictTime.getHour();
        
        // 找到历史上同一星期几同一时段的数据
        return histories.stream()
            .filter(h -> h.getWeekday() != null && h.getWeekday().equals(dayOfWeek) &&
                        h.getHour() != null && h.getHour().equals(hour))
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
    }

    /**
     * 高级置信度计算
     */
    private double calculateAdvancedConfidence(Long parkingLotId, LocalDateTime predictTime, 
                                             List<ParkingUsageHistory> historicalData, int hoursAhead) {
        if (historicalData.isEmpty()) {
            return 0.5; // 默认置信度
        }
        
        // 基于数据量的置信度
        double dataVolumeConfidence = Math.min(1.0, historicalData.size() / 100.0);
        
        // 基于数据稳定性的置信度
        double stabilityConfidence = calculateDataStability(historicalData);
        
        // 基于预测时间的置信度
        double timeConfidence = Math.max(0.3, 0.9 - hoursAhead * 0.02);
        
        // 基于历史预测准确性的置信度
        double accuracyConfidence = calculateHistoricalAccuracy(parkingLotId, historicalData);
        
        // 综合置信度
        return (dataVolumeConfidence * 0.2 + stabilityConfidence * 0.3 + 
                timeConfidence * 0.3 + accuracyConfidence * 0.2);
    }

    /**
     * 计算数据稳定性
     */
    private double calculateDataStability(List<ParkingUsageHistory> histories) {
        if (histories.size() < 2) return 0.5;
        
        double mean = histories.stream()
            .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
            .average()
            .orElse(50.0);
        
        double variance = histories.stream()
            .mapToDouble(h -> {
                double value = h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0;
                return Math.pow(value - mean, 2);
            })
            .average()
            .orElse(0.0);
        
        // 方差越小，稳定性越高
        return Math.max(0.3, Math.min(1.0, 1.0 - variance / 1000.0));
    }

    /**
     * 计算历史预测准确性
     */
    private double calculateHistoricalAccuracy(Long parkingLotId, List<ParkingUsageHistory> historicalData) {
        // 这里可以实现历史预测准确性的计算
        // 暂时返回默认值
        return 0.75;
    }

    /**
     * 高级提示生成
     */
    private String generateAdvancedTip(double predictedRate, LocalDateTime predictTime) {
        int hour = predictTime.getHour();
        int dayOfWeek = predictTime.getDayOfWeek().getValue();
        
        if (predictedRate >= 0.85) {
            if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
                return "高峰时段车位极度紧张，强烈建议提前1小时预约或选择其他停车场";
            } else {
                return "车位紧张，建议提前预约或选择其他停车场";
            }
        } else if (predictedRate >= 0.6) {
            if (dayOfWeek >= 1 && dayOfWeek <= 5 && (hour >= 8 && hour <= 18)) {
                return "工作日时段车位一般，建议提前30分钟到达";
            } else {
                return "车位一般，建议早点到达";
            }
        } else if (predictedRate >= 0.3) {
            return "车位较充足，可放心前往";
        } else {
            return "车位充足，停车无忧";
        }
    }

    /**
     * 计算增强费率（基于时间和星期调整）
     */
    private Double calculateEnhancedRate(Double baseRate, LocalDateTime predictTime) {
        int hour = predictTime.getHour();
        int dayOfWeek = predictTime.getDayOfWeek().getValue();
        
        // 基于时段的调整因子
        double[] hourlyFactors = {
            0.4, 0.3, 0.25, 0.2, 0.3, 0.5,  // 0-5
            0.7, 0.9, 1.0, 0.95, 0.85, 0.8,    // 6-11
            0.75, 0.7, 0.72, 0.78, 0.85, 0.9,  // 12-17
            0.95, 1.0, 0.9, 0.8, 0.6, 0.45     // 18-23
        };
        
        // 基于星期的调整因子
        double weeklyFactor = 1.0;
        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            // 工作日
            weeklyFactor = 1.1;
        } else {
            // 周末
            weeklyFactor = 0.85;
        }
        
        // 特殊时段调整
        double specialFactor = 1.0;
        if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
            specialFactor = 1.2; // 高峰时段
        } else if (hour >= 22 || hour <= 6) {
            specialFactor = 0.7; // 夜间时段
        }
        
        double adjustedRate = baseRate * hourlyFactors[hour] * weeklyFactor * specialFactor;
        return Math.max(5.0, Math.min(95.0, adjustedRate));
    }

    private boolean isWeekend(LocalDateTime time) {
        int dayOfWeek = time.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    private double calculateConfidence(int hoursAhead) {
        // Simple confidence calculation based on prediction horizon
        double baseConfidence = 0.85;
        double decay = hoursAhead * 0.02;
        return Math.max(0.5, Math.min(0.95, baseConfidence - decay));
    }

    private String generateTip(double predictedRate) {
        int hour = LocalDateTime.now().getHour();
        
        if (predictedRate >= 0.85) {
            if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
                return "高峰时段车位紧张，强烈建议提前预约";
            } else {
                return "车位紧张，建议提前预约或选择其他停车场";
            }
        } else if (predictedRate >= 0.6) {
            if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
                return "高峰时段车位一般，建议早点到达";
            } else {
                return "车位一般，建议早点到达";
            }
        } else if (predictedRate >= 0.3) {
            return "车位较充足，可放心前往";
        } else {
            return "车位充足，停车无忧";
        }
    }
}