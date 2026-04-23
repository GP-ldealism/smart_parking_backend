package cn.gp.smartparking.controller;

import cn.gp.smartparking.algorithm.ParkingOccupancyPredictorService;
import cn.gp.smartparking.algorithm.PredictionResult;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingPrediction;
import cn.gp.smartparking.service.ParkingPredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prediction")
@Tag(name = "预测分析")
public class PredictionController {

    @Resource
    private ParkingPredictionService parkingPredictionService;

    @Resource
    private ParkingOccupancyPredictorService predictorService;

    @Operation(summary = "获取停车场未来24小时预测")
    @GetMapping("/parkingLot/{parkingLotId}/24h")
    public Result<List<ParkingPrediction>> get24hPrediction(@PathVariable Long parkingLotId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(24);
        
        List<ParkingPrediction> predictions = parkingPredictionService.lambdaQuery()
                .eq(ParkingPrediction::getParkingLotId, parkingLotId)
                .ge(ParkingPrediction::getPredictTime, now)
                .le(ParkingPrediction::getPredictTime, endTime)
                .orderByAsc(ParkingPrediction::getPredictTime)
                .list();
        
        return Result.success("获取24小时预测成功", predictions);
    }

    @Operation(summary = "获取特定时间点的预测")
    @GetMapping("/parkingLot/{parkingLotId}/time")
    public Result<ParkingPrediction> getTimePrediction(
            @PathVariable Long parkingLotId,
            @RequestParam LocalDateTime predictTime) {
        
        ParkingPrediction prediction = parkingPredictionService.lambdaQuery()
                .eq(ParkingPrediction::getParkingLotId, parkingLotId)
                .eq(ParkingPrediction::getPredictTime, predictTime)
                .one();
        
        return Result.success("获取时间点预测成功", prediction);
    }

    @Operation(summary = "获取最佳停车时间推荐")
    @GetMapping("/parking-lot/{parkingLotId}/best-time")
    public Result<List<ParkingPrediction>> getBestParkingTime(@PathVariable Long parkingLotId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusDays(1);
        
        // 查询未来24小时的预测数据
        List<ParkingPrediction> predictions = parkingPredictionService.lambdaQuery()
                .eq(ParkingPrediction::getParkingLotId, parkingLotId)
                .ge(ParkingPrediction::getPredictTime, now)
                .le(ParkingPrediction::getPredictTime, endTime)
                .orderByAsc(ParkingPrediction::getOccupancyRate)
                .list();
        
        // 返回占用率最低的前3个时间段
        List<ParkingPrediction> bestTimes = predictions.size() > 3 ? 
                predictions.subList(0, 3) : predictions;
        
        return Result.success("获取最佳停车时间成功", bestTimes);
    }

    @Operation(summary = "获取所有停车场的预测概览")
    @GetMapping("/overview")
    public Result<List<ParkingPrediction>> getPredictionOverview(
            @RequestParam LocalDateTime predictTime) {
        
        List<ParkingPrediction> predictions = parkingPredictionService.lambdaQuery()
                .eq(ParkingPrediction::getPredictTime, predictTime)
                .orderByAsc(ParkingPrediction::getOccupancyRate)
                .list();
        
        return Result.success("获取预测概览成功", predictions);
    }

    @Operation(summary = "基于算法的动态预测 - 加权移动平均模型")
    @GetMapping("/algorithm/{parkingLotId}/wma")
    public Result<List<PredictionResult>> predictWithWMA(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        List<PredictionResult> results = predictorService.predictWithModel(
            parkingLotId, startTime, hours, "wma");
        
        return Result.success("加权移动平均预测成功", results);
    }

    @Operation(summary = "基于算法的动态预测 - 指数平滑模型")
    @GetMapping("/algorithm/{parkingLotId}/es")
    public Result<List<PredictionResult>> predictWithES(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        List<PredictionResult> results = predictorService.predictWithModel(
            parkingLotId, startTime, hours, "es");
        
        return Result.success("指数平滑预测成功", results);
    }

    @Operation(summary = "集成预测 - 融合多种算法结果")
    @GetMapping("/algorithm/{parkingLotId}/ensemble")
    public Result<Object> predictEnsemble(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        ParkingOccupancyPredictorService.EnsemblePredictionResult ensembleResult = 
            predictorService.predictEnsemble(parkingLotId, startTime, hours);
        
        return Result.success("集成预测成功", ensembleResult);
    }

    @Operation(summary = "最佳停车时间推荐")
    @GetMapping("/algorithm/{parkingLotId}/recommend-times")
    public Result<List<?>> recommendBestParkingTimes(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        var recommendations = predictorService.recommendBestParkingTimes(
            parkingLotId, startTime, hours);
        
        return Result.success("获取最佳停车时间推荐成功", recommendations);
    }

    @Operation(summary = "预测模型对比分析")
    @GetMapping("/algorithm/{parkingLotId}/compare")
    public Result<?> compareModels(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        var comparison = predictorService.compareModels(parkingLotId, startTime, hours);
        
        return Result.success("模型对比分析成功", comparison);
    }

    @Operation(summary = "预测摘要信息")
    @GetMapping("/algorithm/{parkingLotId}/summary")
    public Result<List<Map<String, Object>>> getPredictionSummary(@PathVariable Long parkingLotId) {
        List<Map<String, Object>> summary = predictorService.getPredictionSummary(parkingLotId);
        return Result.success("获取预测摘要成功", summary);
    }
}