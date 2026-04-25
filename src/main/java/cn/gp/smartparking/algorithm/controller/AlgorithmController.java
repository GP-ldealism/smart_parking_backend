package cn.gp.smartparking.algorithm.controller;

import cn.gp.smartparking.algorithm.entity.PredictionResult;
import cn.gp.smartparking.algorithm.entity.RecommendRequest;
import cn.gp.smartparking.algorithm.entity.RecommendResult;
import cn.gp.smartparking.algorithm.schedule.DataSimulationSchedule;
import cn.gp.smartparking.algorithm.service.DataSimulationService;
import cn.gp.smartparking.algorithm.service.TimeSeriesPredictorService;
import cn.gp.smartparking.algorithm.service.WeightedRecommendationService;
import cn.gp.smartparking.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/algorithm")
@RequiredArgsConstructor
public class AlgorithmController {

    private final WeightedRecommendationService weightedRecommendationService;
    private final TimeSeriesPredictorService timeSeriesPredictorService;
    private final DataSimulationService dataSimulationService;
    private final DataSimulationSchedule dataSimulationSchedule;

    @PostMapping("/recommend")
    public Result<List<RecommendResult>> recommend(@RequestBody RecommendRequest request) {
        List<RecommendResult> results = weightedRecommendationService.recommend(request);
        return Result.success("推荐成功", results);
    }

    @GetMapping("/recommend")
    public Result<List<RecommendResult>> recommendGet(
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "5") Integer count) {
        RecommendRequest request = new RecommendRequest();
        request.setLongitude(longitude);
        request.setLatitude(latitude);
        request.setUserId(userId);
        request.setRecommendCount(count);
        List<RecommendResult> results = weightedRecommendationService.recommend(request);
        return Result.success("推荐成功", results);
    }

    @GetMapping("/prediction/overview")
    public Result<List<PredictionResult>> getPredictionOverview() {
        List<PredictionResult> results = timeSeriesPredictorService.predict(null, 24);
        return Result.success("预测概览获取成功", results);
    }

    @GetMapping("/prediction/{parkingLotId}")
    public Result<List<PredictionResult>> predict(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "24") Integer hoursAhead) {
        List<PredictionResult> results = timeSeriesPredictorService.predict(parkingLotId, hoursAhead);
        return Result.success("预测成功", results);
    }

    @GetMapping("/prediction/nextHour/{parkingLotId}")
    public Result<PredictionResult> predictNextHour(@PathVariable Long parkingLotId) {
        PredictionResult result = timeSeriesPredictorService.predictNextHour(parkingLotId);
        return result != null ? Result.success("下一小时预测成功", result) : Result.fail("预测失败");
    }

    @GetMapping("/prediction/history")
    public Result<Map<String, Object>> getPredictionHistory(
            @RequestParam(required = false) Long parkingLotId,
            @RequestParam(defaultValue = "168") Integer hours) {
        List<PredictionResult> results = timeSeriesPredictorService.predict(parkingLotId, hours);
        return Result.success("预测历史获取成功", Map.of(
                "predictions", results,
                "modelType", "ARIMA-TimeSeries",
                "dataSource", "parking_usage_history"
        ));
    }

    @PostMapping("/data/generate")
    public Result<String> generateHistoricalData(@RequestParam(defaultValue = "30") Integer days) {
        try {
            dataSimulationService.generateHistoricalData(days);
            return Result.success("历史数据生成成功", "已生成" + days + "天的历史数据");
        } catch (Exception e) {
            return Result.fail("历史数据生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/data/initialize")
    public Result<String> initializeData() {
        try {
            dataSimulationSchedule.generateInitialData();
            return Result.success("初始数据生成成功", "已生成30天的初始历史数据");
        } catch (Exception e) {
            return Result.fail("初始数据生成失败: " + e.getMessage());
        }
    }
}