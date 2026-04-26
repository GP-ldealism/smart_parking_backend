package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingSpaceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parking-space")
@Tag(name = "车位管理")
public class ParkingSpaceController {

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Operation(summary = "分页查询车位列表")
    @GetMapping("/page")
    public Result<IPage<ParkingSpace>> getSpacePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long parkingLotId,
            @RequestParam(required = false) String spaceNo,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status) {
        Page<ParkingSpace> page = new Page<>(current, size);
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        
        if (parkingLotId != null) {
            wrapper.eq(ParkingSpace::getParkingLotId, parkingLotId);
        }
        if (spaceNo != null && !spaceNo.isEmpty()) {
            wrapper.like(ParkingSpace::getSpaceNo, spaceNo);
        }
        if (type != null) {
            wrapper.eq(ParkingSpace::getType, type);
        }
        if (status != null) {
            wrapper.eq(ParkingSpace::getStatus, status);
        }
        
        wrapper.eq(ParkingSpace::getIsDeleted, 0);
        wrapper.orderByAsc(ParkingSpace::getSpaceNo);
        
        IPage<ParkingSpace> result = parkingSpaceService.page(page, wrapper);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "根据停车场ID查询车位列表")
    @GetMapping("/list/{parkingLotId}")
    public Result<IPage<ParkingSpace>> getSpacesByLotId(
            @PathVariable Long parkingLotId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String spaceNo) {
        Page<ParkingSpace> page = new Page<>(current, size);
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getParkingLotId, parkingLotId);
        if (spaceNo != null && !spaceNo.isEmpty()) {
            wrapper.like(ParkingSpace::getSpaceNo, spaceNo);
        }
        wrapper.eq(ParkingSpace::getIsDeleted, 0);
        wrapper.orderByAsc(ParkingSpace::getSpaceNo);

        IPage<ParkingSpace> result = parkingSpaceService.page(page, wrapper);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "获取车位详情")
    @GetMapping("/{id}")
    public Result<ParkingSpace> getSpaceById(@PathVariable Long id) {
        ParkingSpace parkingSpace = parkingSpaceService.getById(id);
        if (parkingSpace == null || parkingSpace.getIsDeleted() == 1) {
            return Result.fail("车位不存在");
        }
        return Result.success("查询成功", parkingSpace);
    }

    @Operation(summary = "创建车位")
    @PostMapping
    @Log
    public Result<ParkingSpace> createSpace(@RequestBody ParkingSpace parkingSpace) {
        // 检查车位编号是否重复
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getParkingLotId, parkingSpace.getParkingLotId());
        wrapper.eq(ParkingSpace::getSpaceNo, parkingSpace.getSpaceNo());
        wrapper.eq(ParkingSpace::getIsDeleted, 0);
        
        if (parkingSpaceService.count(wrapper) > 0) {
            return Result.fail("车位编号已存在");
        }
        
        parkingSpace.setIsDeleted(0);
        boolean success = parkingSpaceService.save(parkingSpace);
        if (success) {
            return Result.success("创建成功", parkingSpace);
        } else {
            return Result.fail("创建失败");
        }
    }

    @Operation(summary = "更新车位")
    @PutMapping("/{id}")
    @Log
    public Result<ParkingSpace> updateSpace(@PathVariable Long id, @RequestBody ParkingSpace parkingSpace) {
        ParkingSpace existingSpace = parkingSpaceService.getById(id);
        if (existingSpace == null || existingSpace.getIsDeleted() == 1) {
            return Result.fail("车位不存在");
        }
        
        // 如果修改了车位编号，检查是否重复
        if (!existingSpace.getSpaceNo().equals(parkingSpace.getSpaceNo())) {
            LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ParkingSpace::getParkingLotId, parkingSpace.getParkingLotId());
            wrapper.eq(ParkingSpace::getSpaceNo, parkingSpace.getSpaceNo());
            wrapper.eq(ParkingSpace::getIsDeleted, 0);
            wrapper.ne(ParkingSpace::getId, id);
            
            if (parkingSpaceService.count(wrapper) > 0) {
                return Result.fail("车位编号已存在");
            }
        }
        
        parkingSpace.setId(id);
        boolean success = parkingSpaceService.updateById(parkingSpace);
        if (success) {
            return Result.success("更新成功", parkingSpace);
        } else {
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "删除车位")
    @DeleteMapping("/{id}")
    @Log
    public Result<Void> deleteSpace(@PathVariable Long id) {
        ParkingSpace parkingSpace = parkingSpaceService.getById(id);
        if (parkingSpace == null || parkingSpace.getIsDeleted() == 1) {
            return Result.fail("车位不存在");
        }
        
        parkingSpace.setIsDeleted(1);
        boolean success = parkingSpaceService.updateById(parkingSpace);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "批量删除车位")
    @DeleteMapping("/batch")
    @Log
    public Result<Void> batchDeleteSpaces(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            ParkingSpace parkingSpace = parkingSpaceService.getById(id);
            if (parkingSpace != null && parkingSpace.getIsDeleted() == 0) {
                parkingSpace.setIsDeleted(1);
                parkingSpaceService.updateById(parkingSpace);
            }
        }
        return Result.success("批量删除成功");
    }

    @Operation(summary = "更新车位状态")
    @PutMapping("/{id}/status")
    @Log
    public Result<Void> updateSpaceStatus(@PathVariable Long id, @RequestParam Integer status) {
        ParkingSpace parkingSpace = parkingSpaceService.getById(id);
        if (parkingSpace == null || parkingSpace.getIsDeleted() == 1) {
            return Result.fail("车位不存在");
        }
        
        parkingSpace.setStatus(status);
        boolean success = parkingSpaceService.updateById(parkingSpace);
        if (success) {
            return Result.success("状态更新成功");
        } else {
            return Result.fail("状态更新失败");
        }
    }

    @Operation(summary = "获取车位统计信息")
    @GetMapping("/stats/{parkingLotId}")
    public Result<Map<String, Object>> getSpaceStats(@PathVariable Long parkingLotId) {
        LambdaQueryWrapper<ParkingSpace> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ParkingSpace::getParkingLotId, parkingLotId);
        wrapper.eq(ParkingSpace::getIsDeleted, 0);
        
        List<ParkingSpace> allSpaces = parkingSpaceService.list(wrapper);
        
        int total = allSpaces.size();
        int free = (int) allSpaces.stream().filter(s -> s.getStatus() == 1).count();
        int occupied = total - free;
        
        // 按类型统计
        Map<Integer, Long> typeStats = allSpaces.stream()
                .collect(java.util.stream.Collectors.groupingBy(ParkingSpace::getType, java.util.stream.Collectors.counting()));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("free", free);
        stats.put("occupied", occupied);
        stats.put("typeStats", typeStats);
        
        return Result.success("查询成功", stats);
    }
}
