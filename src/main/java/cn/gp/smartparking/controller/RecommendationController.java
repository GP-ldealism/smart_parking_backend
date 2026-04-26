package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.dto.RecommendationResultDTO;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.UserPreference;
import cn.gp.smartparking.service.ParkingLotService;
import cn.gp.smartparking.service.ParkingSpaceService;
import cn.gp.smartparking.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/recommendation")
@Tag(name = "智能推荐")
public class RecommendationController {

    @Resource
    private ParkingLotService parkingLotService;

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Resource
    private UserPreferenceService userPreferenceService;

    @Operation(summary = "基于用户偏好的车位推荐")
    @GetMapping("/parkingLots")
    public Result<List<RecommendationResultDTO>> recommendParkingLots(
            @RequestParam("longitude") Double longitude,
            @RequestParam("latitude") Double latitude,
            @RequestParam(required = false) Long userId) {
        log.info("收到推荐请求 - longitude: {}, latitude: {}, userId: {}",
                longitude, latitude, userId);

        // 获取所有正常运营的停车场
        List<ParkingLot> parkingLots = parkingLotService.lambdaQuery()
                .eq(ParkingLot::getStatus, 1)
                .list();

        // 获取用户偏好
        UserPreference preference = null;
        if (userId != null) {
            preference = userPreferenceService.lambdaQuery()
                    .eq(UserPreference::getUserId, userId)
                    .one();
        }

        // 计算每个停车场的推荐分数并构建推荐结果
        List<RecommendationResultDTO> recommendationResults = new ArrayList<>();
        for (ParkingLot lot : parkingLots) {
            double distance = calculateDistance(lot, longitude, latitude);
            
            // 过滤掉距离超过50公里的停车场
            if (distance > 50000) {
                continue;
            }
            
            double score = calculateRecommendationScore(lot, longitude, latitude, preference);
            double freeSpaceRate = calculateFreeSpaceRate(lot);
            String reason = generateRecommendationReason(lot, distance, freeSpaceRate, score);
            
            RecommendationResultDTO dto = new RecommendationResultDTO();
            dto.setId(lot.getId());
            dto.setName(lot.getName());
            dto.setAddress(lot.getAddress());
            dto.setLongitude(lot.getLongitude());
            dto.setLatitude(lot.getLatitude());
            dto.setTotalSpace(lot.getTotalSpace());
            dto.setFreeSpace(lot.getFreeSpace());
            dto.setRate(lot.getRate());
            dto.setOpenTime(lot.getOpenTime());
            dto.setStatus(lot.getStatus());
            dto.setScore(score * 100); // 转换为0-100分
            dto.setDistance(distance);
            dto.setFreeSpaceRate(freeSpaceRate);
            dto.setReason(reason);
            
            recommendationResults.add(dto);
        }

        // 按推荐分数排序
        recommendationResults.sort(Comparator.comparingDouble(RecommendationResultDTO::getScore).reversed());

        // 只返回前10个推荐结果
        List<RecommendationResultDTO> result = recommendationResults.stream()
                .limit(10)
                .collect(Collectors.toList());

        return Result.success("推荐成功", result);
    }

    /**
     * 计算推荐分数（加权评分算法）
     */
    private double calculateRecommendationScore(ParkingLot lot, Double longitude, Double latitude, UserPreference preference) {
        double score = 0.0;

        // 1. 距离因素（50%权重）- 提高距离权重
        double distanceScore = calculateDistanceScore(lot, longitude, latitude);
        score += distanceScore * 0.5;

        // 2. 空闲车位因素（25%权重）
        double freeSpaceScore = calculateFreeSpaceScore(lot);
        score += freeSpaceScore * 0.25;

        // 3. 价格因素（15%权重）
        double priceScore = calculatePriceScore(lot);
        score += priceScore * 0.15;

        // 4. 用户偏好因素（10%权重）
        if (preference != null) {
            double preferenceScore = calculatePreferenceScore(lot, preference);
            score += preferenceScore * 0.1;
        }

        return score;
    }

    /**
     * 计算距离分数（距离越近分数越高）
     */
    private double calculateDistanceScore(ParkingLot lot, Double longitude, Double latitude) {
        if (lot.getLongitude() == null || lot.getLatitude() == null) {
            return 0.5; // 默认中等分数
        }

        // 计算欧几里得距离（简化处理）
        double distance = Math.sqrt(
                Math.pow(lot.getLongitude().doubleValue() - longitude, 2) +
                Math.pow(lot.getLatitude().doubleValue() - latitude, 2)
        );

        // 转换为米（1度约111公里）
        double distanceInMeters = distance * 111000;

        // 使用指数衰减函数，距离越近分数越高
        // 1公里内：1.0分
        // 5公里内：0.8分
        // 10公里内：0.6分
        // 20公里内：0.4分
        // 50公里内：0.2分
        if (distanceInMeters <= 1000) {
            return 1.0;
        } else if (distanceInMeters <= 5000) {
            return 0.8;
        } else if (distanceInMeters <= 10000) {
            return 0.6;
        } else if (distanceInMeters <= 20000) {
            return 0.4;
        } else if (distanceInMeters <= 50000) {
            return 0.2;
        } else {
            return 0.0;
        }
    }

    /**
     * 计算实际距离（米）
     */
    private double calculateDistance(ParkingLot lot, Double longitude, Double latitude) {
        if (lot.getLongitude() == null || lot.getLatitude() == null) {
            return 0.0;
        }
        
        // 计算欧几里得距离（简化处理）
        double distance = Math.sqrt(
                Math.pow(lot.getLongitude().doubleValue() - longitude, 2) +
                Math.pow(lot.getLatitude().doubleValue() - latitude, 2)
        );
        
        // 转换为米（1度约111公里）
        return distance * 111000;
    }

    /**
     * 计算空闲率
     */
    private double calculateFreeSpaceRate(ParkingLot lot) {
        if (lot.getTotalSpace() == null || lot.getTotalSpace() == 0) {
            return 0.0;
        }
        return (double) lot.getFreeSpace() / lot.getTotalSpace();
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendationReason(ParkingLot lot, double distance, double freeSpaceRate, double score) {
        List<String> reasons = new ArrayList<>();
        
        // 距离因素
        if (distance <= 500) {
            reasons.add("距离最近");
        } else if (distance <= 1000) {
            reasons.add("距离较近");
        }
        
        // 价格因素
        if (lot.getRate() != null && lot.getRate().doubleValue() <= 8) {
            reasons.add("价格实惠");
        }
        
        // 空闲车位因素
        if (freeSpaceRate >= 0.5) {
            reasons.add("空闲车位充足");
        } else if (freeSpaceRate >= 0.3) {
            reasons.add("空闲车位较多");
        }
        
        // 综合评分因素
        if (score >= 0.8) {
            reasons.add("综合评分高");
        }
        
        return reasons.isEmpty() ? "推荐停车场" : String.join(" · ", reasons);
    }

    /**
     * 计算空闲车位分数（空闲率越高分数越高）
     */
    private double calculateFreeSpaceScore(ParkingLot lot) {
        if (lot.getTotalSpace() == 0) {
            return 0;
        }
        return (double) lot.getFreeSpace() / lot.getTotalSpace();
    }

    /**
     * 计算价格分数（价格越低分数越高）
     */
    private double calculatePriceScore(ParkingLot lot) {
        // 假设最高价格为20元/小时
        double maxPrice = 20.0;
        return Math.max(0, 1 - lot.getRate().doubleValue() / maxPrice);
    }

    /**
     * 计算用户偏好分数
     */
    private double calculatePreferenceScore(ParkingLot lot, UserPreference preference) {
        double score = 0.0;
        
        // 根据用户偏好的车位类型进行匹配
        // 这里简化处理，实际应该查询该停车场是否有用户偏好的车位类型
        
        return score;
    }
}