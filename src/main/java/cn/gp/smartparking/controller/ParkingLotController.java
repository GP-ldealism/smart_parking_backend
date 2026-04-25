package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingLotService;
import cn.gp.smartparking.service.ParkingSpaceService;
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
@RequestMapping("/parkingLot")
@Tag(name = "停车场管理")
public class ParkingLotController {

    @Resource
    private ParkingLotService parkingLotService;

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Operation(summary = "获取停车场列表")
    @GetMapping("/list")
    public Result<Map<String, Object>> getParkingLotList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // 查询总数
        long total = parkingLotService.lambdaQuery()
                .eq(ParkingLot::getIsDeleted, 0)
                .count();
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 使用原生SQL查询分页数据
        List<ParkingLot> records = parkingLotService.getBaseMapper().selectList(
                Wrappers.query(new ParkingLot())
                        .eq("is_deleted", 0)
                        .orderByAsc("id")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );
        
        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);
        
        return Result.success("获取停车场列表成功", response);
    }

    @Operation(summary = "获取停车场详情")
    @GetMapping("/{id}")
    public Result<ParkingLot> getParkingLotDetail(@PathVariable Long id) {
        ParkingLot parkingLot = parkingLotService.getById(id);
        return Result.success("获取停车场详情成功", parkingLot);
    }

    @Operation(summary = "获取停车场车位状态")
    @GetMapping("/{id}/spaces")
    public Result<List<ParkingSpace>> getParkingSpaces(@PathVariable Long id) {
        List<ParkingSpace> spaces = parkingSpaceService.lambdaQuery()
                .eq(ParkingSpace::getParkingLotId, id)
                .list();
        return Result.success("获取车位状态成功", spaces);
    }

    @Operation(summary = "获取停车场空闲车位")
    @GetMapping("/{id}/free-spaces")
    public Result<List<ParkingSpace>> getFreeSpaces(@PathVariable Long id) {
        List<ParkingSpace> freeSpaces = parkingSpaceService.lambdaQuery()
                .eq(ParkingSpace::getParkingLotId, id)
                .eq(ParkingSpace::getStatus, 1) // 1表示空闲
                .list();
        return Result.success("获取空闲车位成功", freeSpaces);
    }

    @Operation(summary = "根据位置查询附近停车场")
    @GetMapping("/nearby")
    public Result<List<ParkingLot>> getNearbyParkingLots(
            @RequestParam Double longitude,
            @RequestParam Double latitude,
            @RequestParam(defaultValue = "5000") Integer radius) {
        // 这里简化处理，实际应该根据经纬度计算距离
        List<ParkingLot> parkingLots = parkingLotService.lambdaQuery()
                .eq(ParkingLot::getStatus, 1) // 只查询正常运营的停车场
                .list();
        return Result.success("获取附近停车场成功", parkingLots);
    }

    @Operation(summary = "根据名称模糊搜索停车场")
    @GetMapping("/search")
    public Result<List<ParkingLot>> searchParkingLots(
            @RequestParam String name,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double latitude) {
        List<ParkingLot> parkingLots = parkingLotService.lambdaQuery()
                .like(ParkingLot::getName, name)
                .eq(ParkingLot::getStatus, 1)
                .eq(ParkingLot::getIsDeleted, 0)
                .list();
        return Result.success("搜索停车场成功", parkingLots);
    }

    @Operation(summary = "创建停车场（管理员功能）")
    @PostMapping
    public Result<Long> createParkingLot(@RequestBody ParkingLot parkingLot) {
        parkingLotService.save(parkingLot);
        return Result.success("创建停车场成功", parkingLot.getId());
    }

    @Operation(summary = "更新停车场信息（管理员功能）")
    @PutMapping("/{id}")
    public Result<Void> updateParkingLot(@PathVariable Long id, @RequestBody ParkingLot parkingLot) {
        parkingLot.setId(id);
        parkingLotService.updateById(parkingLot);
        return Result.success("更新停车场信息成功");
    }

    @Operation(summary = "删除停车场（软删除，管理员功能）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteParkingLot(@PathVariable Long id) {
        boolean success = parkingLotService.removeById(id);
        if (success) {
            return Result.success("删除停车场成功");
        }
        return Result.fail("删除停车场失败");
    }

    @Operation(summary = "更新停车场状态（管理员功能）")
    @PutMapping("/{id}/status")
    public Result<Void> updateParkingLotStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        ParkingLot parkingLot = parkingLotService.getById(id);
        if (parkingLot == null) {
            return Result.fail("停车场不存在");
        }
        parkingLot.setStatus(status);
        parkingLotService.updateById(parkingLot);
        return Result.success("更新停车场状态成功");
    }
}