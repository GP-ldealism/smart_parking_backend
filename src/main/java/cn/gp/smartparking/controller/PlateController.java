package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.UserPlate;
import cn.gp.smartparking.service.UserPlateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/plate")
@Tag(name = "车牌管理")
public class PlateController {

    @Resource
    private UserPlateService userPlateService;

    @Operation(summary = "分页查询车牌列表")
    @GetMapping("/page")
    public Result<IPage<UserPlate>> getPlatePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) Integer isDefault,
            @RequestParam(required = false) Integer status) {
        Page<UserPlate> page = new Page<>(current, size);
        LambdaQueryWrapper<UserPlate> wrapper = new LambdaQueryWrapper<>();
        
        if (userId != null) {
            wrapper.eq(UserPlate::getUserId, userId);
        }
        if (plateNumber != null && !plateNumber.isEmpty()) {
            wrapper.like(UserPlate::getPlateNumber, plateNumber);
        }
        if (isDefault != null) {
            wrapper.eq(UserPlate::getIsDefault, isDefault);
        }
        if (status != null) {
            wrapper.eq(UserPlate::getStatus, status);
        }
        
        wrapper.eq(UserPlate::getIsDeleted, 0);
        wrapper.orderByDesc(UserPlate::getCreateTime);
        
        IPage<UserPlate> result = userPlateService.page(page, wrapper);
        return Result.success("查询成功", result);
    }

    @Operation(summary = "获取车牌详情")
    @GetMapping("/{id}")
    public Result<UserPlate> getPlateById(@PathVariable Long id) {
        UserPlate userPlate = userPlateService.getById(id);
        if (userPlate == null || userPlate.getIsDeleted() == 1) {
            return Result.fail("车牌不存在");
        }
        return Result.success("查询成功", userPlate);
    }

    @Operation(summary = "创建车牌")
    @PostMapping
    @Log(module = "车牌管理", operation = "创建车牌")
    public Result<UserPlate> createPlate(@RequestBody UserPlate userPlate) {
        // 检查车牌号是否重复
        LambdaQueryWrapper<UserPlate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlate::getPlateNumber, userPlate.getPlateNumber());
        wrapper.eq(UserPlate::getIsDeleted, 0);
        if (userPlate.getUserId() != null) {
            wrapper.eq(UserPlate::getUserId, userPlate.getUserId());
        }
        UserPlate existing = userPlateService.getOne(wrapper);
        if (existing != null) {
            return Result.fail("车牌号已存在");
        }
        
        userPlate.setIsDeleted(0);
        userPlate.setStatus(1);
        boolean success = userPlateService.save(userPlate);
        if (success) {
            return Result.success("创建成功", userPlate);
        } else {
            return Result.fail("创建失败");
        }
    }

    @Operation(summary = "更新车牌")
    @PutMapping("/{id}")
    @Log(module = "车牌管理", operation = "更新车牌")
    public Result<UserPlate> updatePlate(@PathVariable Long id, @RequestBody UserPlate userPlate) {
        UserPlate existing = userPlateService.getById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            return Result.fail("车牌不存在");
        }
        
        // 检查车牌号是否重复（排除自己）
        LambdaQueryWrapper<UserPlate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPlate::getPlateNumber, userPlate.getPlateNumber());
        wrapper.eq(UserPlate::getIsDeleted, 0);
        wrapper.ne(UserPlate::getId, id);
        if (userPlate.getUserId() != null) {
            wrapper.eq(UserPlate::getUserId, userPlate.getUserId());
        }
        UserPlate duplicate = userPlateService.getOne(wrapper);
        if (duplicate != null) {
            return Result.fail("车牌号已存在");
        }
        
        userPlate.setId(id);
        boolean success = userPlateService.updateById(userPlate);
        if (success) {
            return Result.success("更新成功", userPlate);
        } else {
            return Result.fail("更新失败");
        }
    }

    @Operation(summary = "删除车牌")
    @DeleteMapping("/{id}")
    @Log(module = "车牌管理", operation = "删除车牌")
    public Result<Void> deletePlate(@PathVariable Long id) {
        UserPlate userPlate = userPlateService.getById(id);
        if (userPlate == null || userPlate.getIsDeleted() == 1) {
            return Result.fail("车牌不存在");
        }
        
        userPlate.setIsDeleted(1);
        boolean success = userPlateService.updateById(userPlate);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

    @Operation(summary = "批量删除车牌")
    @PostMapping("/batch-delete")
    @Log(module = "车牌管理", operation = "批量删除车牌")
    public Result<Void> batchDeletePlates(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("请选择要删除的车牌");
        }
        
        boolean success = userPlateService.lambdaUpdate()
                .set(UserPlate::getIsDeleted, 1)
                .in(UserPlate::getId, ids)
                .update();
        if (success) {
            return Result.success("批量删除成功");
        } else {
            return Result.fail("批量删除失败");
        }
    }

    @Operation(summary = "更新车牌状态")
    @PutMapping("/{id}/status")
    @Log(module = "车牌管理", operation = "更新车牌状态")
    public Result<Void> updatePlateStatus(@PathVariable Long id, @RequestParam Integer status) {
        UserPlate userPlate = userPlateService.getById(id);
        if (userPlate == null || userPlate.getIsDeleted() == 1) {
            return Result.fail("车牌不存在");
        }
        
        userPlate.setStatus(status);
        boolean success = userPlateService.updateById(userPlate);
        if (success) {
            return Result.success("状态更新成功");
        } else {
            return Result.fail("状态更新失败");
        }
    }

    @Operation(summary = "设置默认车牌")
    @PutMapping("/{id}/default")
    @Log(module = "车牌管理", operation = "设置默认车牌")
    public Result<Void> setDefaultPlate(@PathVariable Long id) {
        UserPlate userPlate = userPlateService.getById(id);
        if (userPlate == null || userPlate.getIsDeleted() == 1) {
            return Result.fail("车牌不存在");
        }
        
        // 先取消该用户的所有默认车牌
        if (userPlate.getUserId() != null) {
            userPlateService.lambdaUpdate()
                    .set(UserPlate::getIsDefault, 0)
                    .eq(UserPlate::getUserId, userPlate.getUserId())
                    .update();
        }
        
        // 设置新的默认车牌
        userPlate.setIsDefault(1);
        boolean success = userPlateService.updateById(userPlate);
        if (success) {
            return Result.success("设置成功");
        } else {
            return Result.fail("设置失败");
        }
    }

    @Operation(summary = "获取车牌统计信息")
    @GetMapping("/stats")
    public Result<Object> getStats() {
        long total = userPlateService.lambdaQuery()
                .eq(UserPlate::getIsDeleted, 0)
                .count();
        
        long active = userPlateService.lambdaQuery()
                .eq(UserPlate::getIsDeleted, 0)
                .eq(UserPlate::getStatus, 1)
                .count();
        
        long defaultCount = userPlateService.lambdaQuery()
                .eq(UserPlate::getIsDeleted, 0)
                .eq(UserPlate::getIsDefault, 1)
                .count();
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("active", active);
        stats.put("defaultCount", defaultCount);
        stats.put("inactive", total - active);
        
        return Result.success("查询成功", stats);
    }
}
