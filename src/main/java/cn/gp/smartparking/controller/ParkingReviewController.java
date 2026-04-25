package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingReview;
import cn.gp.smartparking.service.ParkingReviewService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parkingReview")
@Tag(name = "停车场评价管理")
public class ParkingReviewController {

    @Resource
    private ParkingReviewService parkingReviewService;

    @Operation(summary = "获取停车场评价列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getParkingReviewList(
            @RequestParam(required = false) Long parkingLotId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 查询总数
        long total = parkingReviewService.lambdaQuery()
                .eq(parkingLotId != null, ParkingReview::getParkingLotId, parkingLotId)
                .eq(userId != null, ParkingReview::getUserId, userId)
                .eq(ParkingReview::getIsDeleted, 0)
                .count();
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 使用原生SQL查询分页数据
        List<ParkingReview> records = parkingReviewService.getBaseMapper().selectList(
                Wrappers.query(new ParkingReview())
                        .eq(parkingLotId != null, "parking_lot_id", parkingLotId)
                        .eq(userId != null, "user_id", userId)
                        .eq("is_deleted", 0)
                        .orderByDesc("create_time")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);
        
        return Result.success("获取评价列表成功", response);
    }

    @Operation(summary = "获取停车场评价详情")
    @GetMapping("/{id}")
    public Result<ParkingReview> getParkingReviewDetail(@PathVariable Long id) {
        ParkingReview parkingReview = parkingReviewService.getById(id);
        return Result.success("获取评价详情成功", parkingReview);
    }

    @Operation(summary = "根据停车场ID获取评价")
    @GetMapping("/parkingLot/{parkingLotId}")
    public Result<List<ParkingReview>> getReviewsByParkingLot(@PathVariable Long parkingLotId) {
        List<ParkingReview> reviews = parkingReviewService.lambdaQuery()
                .eq(ParkingReview::getParkingLotId, parkingLotId)
                .eq(ParkingReview::getIsDeleted, 0)
                .orderByDesc(ParkingReview::getCreateTime)
                .list();
        return Result.success("获取停车场评价成功", reviews);
    }

    @Operation(summary = "根据用户ID获取评价")
    @GetMapping("/user/{userId}")
    public Result<List<ParkingReview>> getReviewsByUser(@PathVariable Long userId) {
        List<ParkingReview> reviews = parkingReviewService.lambdaQuery()
                .eq(ParkingReview::getUserId, userId)
                .eq(ParkingReview::getIsDeleted, 0)
                .orderByDesc(ParkingReview::getCreateTime)
                .list();
        return Result.success("获取用户评价成功", reviews);
    }

    @Log(module = "评价管理", operation = "创建", description = "创建评价")
    @Operation(summary = "创建评价")
    @PostMapping
    public Result<Long> createParkingReview(@RequestBody ParkingReview parkingReview) {
        parkingReviewService.save(parkingReview);
        return Result.success("创建评价成功", parkingReview.getId());
    }

    @Log(module = "评价管理", operation = "更新", description = "更新评价")
    @Operation(summary = "更新评价")
    @PutMapping("/{id}")
    public Result<Void> updateParkingReview(@PathVariable Long id, @RequestBody ParkingReview parkingReview) {
        parkingReview.setId(id);
        parkingReviewService.updateById(parkingReview);
        return Result.success("更新评价成功");
    }

    @Log(module = "评价管理", operation = "删除", description = "删除评价")
    @Operation(summary = "删除评价（软删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteParkingReview(@PathVariable Long id) {
        boolean success = parkingReviewService.removeById(id);
        if (success) {
            return Result.success("删除评价成功");
        }
        return Result.fail("删除评价失败");
    }
}
