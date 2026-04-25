package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import cn.gp.smartparking.service.ParkingUsageHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/parkingUsageHistory")
@Tag(name = "车位历史占用管理")
public class ParkingUsageHistoryController {

    @Resource
    private ParkingUsageHistoryService parkingUsageHistoryService;

    @Operation(summary = "获取历史记录列表")
    @GetMapping("/list")
    public Result<List<ParkingUsageHistory>> getHistoryList(
            @RequestParam(required = false) Long parkingLotId,
            @RequestParam(required = false) Integer hour,
            @RequestParam(required = false) Integer weekday) {
        
        List<ParkingUsageHistory> histories = parkingUsageHistoryService.lambdaQuery()
                .eq(parkingLotId != null, ParkingUsageHistory::getParkingLotId, parkingLotId)
                .eq(hour != null, ParkingUsageHistory::getHour, hour)
                .eq(weekday != null, ParkingUsageHistory::getWeekday, weekday)
                .orderByDesc(ParkingUsageHistory::getRecordTime)
                .list();
        
        return Result.success("获取历史记录列表成功", histories);
    }

    @Operation(summary = "获取历史记录详情")
    @GetMapping("/{id}")
    public Result<ParkingUsageHistory> getHistoryDetail(@PathVariable Long id) {
        ParkingUsageHistory history = parkingUsageHistoryService.getById(id);
        return Result.success("获取历史记录详情成功", history);
    }

    @Log(module = "历史记录管理", operation = "创建", description = "创建历史记录")
    @Operation(summary = "创建历史记录")
    @PostMapping
    public Result<ParkingUsageHistory> createHistory(@RequestBody ParkingUsageHistory history) {
        parkingUsageHistoryService.save(history);
        return Result.success("创建历史记录成功", history);
    }

    @Log(module = "历史记录管理", operation = "更新", description = "更新历史记录")
    @Operation(summary = "更新历史记录")
    @PutMapping("/{id}")
    public Result<ParkingUsageHistory> updateHistory(@PathVariable Long id, @RequestBody ParkingUsageHistory history) {
        history.setId(id);
        parkingUsageHistoryService.updateById(history);
        ParkingUsageHistory updated = parkingUsageHistoryService.getById(id);
        return Result.success("更新历史记录成功", updated);
    }

    @Log(module = "历史记录管理", operation = "删除", description = "删除历史记录")
    @Operation(summary = "删除历史记录")
    @DeleteMapping("/{id}")
    public Result<Void> deleteHistory(@PathVariable Long id) {
        parkingUsageHistoryService.removeById(id);
        return Result.success("删除历史记录成功");
    }

    @Operation(summary = "获取停车场历史统计")
    @GetMapping("/parkingLot/{parkingLotId}/statistics")
    public Result<Object> getParkingLotStatistics(@PathVariable Long parkingLotId) {
        // 统计功能可以根据实际需求实现
        Object statistics = new Object();
        return Result.success("获取停车场历史统计成功", statistics);
    }

    @Operation(summary = "获取时间段统计")
    @GetMapping("/statistics/time-range")
    public Result<List<ParkingUsageHistory>> getTimeRangeStatistics(
            @RequestParam Long parkingLotId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        
        List<ParkingUsageHistory> histories = parkingUsageHistoryService.lambdaQuery()
                .eq(ParkingUsageHistory::getParkingLotId, parkingLotId)
                .ge(ParkingUsageHistory::getRecordTime, startTime)
                .le(ParkingUsageHistory::getRecordTime, endTime)
                .orderByAsc(ParkingUsageHistory::getRecordTime)
                .list();
        
        return Result.success("获取时间段统计成功", histories);
    }

    @Log(module = "历史记录管理", operation = "批量导入", description = "批量导入历史记录")
    @Operation(summary = "批量导入历史记录")
    @PostMapping("/batch")
    public Result<Void> batchImportHistory(@RequestBody List<ParkingUsageHistory> histories) {
        parkingUsageHistoryService.saveBatch(histories);
        return Result.success("批量导入历史记录成功");
    }
}