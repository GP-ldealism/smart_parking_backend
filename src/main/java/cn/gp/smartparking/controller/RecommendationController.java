package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
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
    public Result<List<ParkingLot>> recommendParkingLots(
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

        // 计算每个停车场的推荐分数
        List<RecommendedParkingLot> recommendedList = new ArrayList<>();
        for (ParkingLot lot : parkingLots) {
            double score = calculateRecommendationScore(lot, longitude, latitude, preference);
            recommendedList.add(new RecommendedParkingLot(lot, score));
        }

        // 按推荐分数排序
        recommendedList.sort(Comparator.comparingDouble(RecommendedParkingLot::getScore).reversed());

        // 提取排序后的停车场列表
        List<ParkingLot> result = recommendedList.stream()
                .map(RecommendedParkingLot::getParkingLot)
                .toList();

        return Result.success("推荐成功", result);
    }

    /**
     * 计算推荐分数（加权评分算法）
     */
    private double calculateRecommendationScore(ParkingLot lot, Double longitude, Double latitude, UserPreference preference) {
        double score = 0.0;

        // 1. 距离因素（30%权重）
        double distanceScore = calculateDistanceScore(lot, longitude, latitude);
        score += distanceScore * 0.3;

        // 2. 空闲车位因素（30%权重）
        double freeSpaceScore = calculateFreeSpaceScore(lot);
        score += freeSpaceScore * 0.3;

        // 3. 价格因素（20%权重）
        double priceScore = calculatePriceScore(lot);
        score += priceScore * 0.2;

        // 4. 用户偏好因素（20%权重）
        if (preference != null) {
            double preferenceScore = calculatePreferenceScore(lot, preference);
            score += preferenceScore * 0.2;
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
        
        // 距离越近分数越高（最大距离设为1度约111公里）
        double maxDistance = 0.1; // 约11公里
        return Math.max(0, 1 - distance / maxDistance);
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

    /**
     * 推荐停车场内部类
     */
    private static class RecommendedParkingLot {
        private ParkingLot parkingLot;
        private double score;

        public RecommendedParkingLot(ParkingLot parkingLot, double score) {
            this.parkingLot = parkingLot;
            this.score = score;
        }

        public ParkingLot getParkingLot() {
            return parkingLot;
        }

        public double getScore() {
            return score;
        }
    }
}