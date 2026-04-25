package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.Blacklist;
import cn.gp.smartparking.service.BlacklistService;
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
@RequestMapping("/blacklist")
@Tag(name = "黑名单管理")
public class BlacklistController {

    @Resource
    private BlacklistService blacklistService;

    @Operation(summary = "获取黑名单列表")
    @GetMapping("/list")
    public Result<List<Blacklist>> getBlacklist(
            @RequestParam(required = false) String plateNumber) {
        
        List<Blacklist> blacklists = blacklistService.lambdaQuery()
                .like(plateNumber != null, Blacklist::getPlateNumber, plateNumber)
                .orderByDesc(Blacklist::getCreateTime)
                .list();
        
        return Result.success("获取黑名单列表成功", blacklists);
    }

    @Operation(summary = "获取黑名单详情")
    @GetMapping("/{id}")
    public Result<Blacklist> getBlacklistDetail(@PathVariable Long id) {
        Blacklist blacklist = blacklistService.getById(id);
        return Result.success("获取黑名单详情成功", blacklist);
    }

    @Log(module = "黑名单管理", operation = "添加", description = "添加黑名单")
    @Operation(summary = "添加黑名单")
    @PostMapping
    public Result<Blacklist> addBlacklist(@RequestBody Blacklist blacklist) {
        blacklistService.save(blacklist);
        return Result.success("添加黑名单成功", blacklist);
    }

    @Log(module = "黑名单管理", operation = "更新", description = "更新黑名单")
    @Operation(summary = "更新黑名单")
    @PutMapping("/{id}")
    public Result<Blacklist> updateBlacklist(@PathVariable Long id, @RequestBody Blacklist blacklist) {
        blacklist.setId(id);
        blacklistService.updateById(blacklist);
        Blacklist updated = blacklistService.getById(id);
        return Result.success("更新黑名单成功", updated);
    }

    @Log(module = "黑名单管理", operation = "移除", description = "移除黑名单")
    @Operation(summary = "移除黑名单")
    @DeleteMapping("/{id}")
    public Result<Void> removeBlacklist(@PathVariable Long id) {
        blacklistService.removeById(id);
        return Result.success("移除黑名单成功");
    }

    @Operation(summary = "查询车牌是否在黑名单")
    @GetMapping("/check/{plateNumber}")
    public Result<Boolean> checkPlateInBlacklist(@PathVariable String plateNumber) {
        Blacklist blacklist = blacklistService.lambdaQuery()
                .eq(Blacklist::getPlateNumber, plateNumber)
                .one();
        boolean isInBlacklist = blacklist != null;
        return Result.success("查询车牌是否在黑名单成功", isInBlacklist);
    }

    @Log(module = "黑名单管理", operation = "批量添加", description = "批量添加黑名单")
    @Operation(summary = "批量添加黑名单")
    @PostMapping("/batch")
    public Result<Void> batchAddBlacklist(@RequestBody List<Blacklist> blacklists) {
        blacklistService.saveBatch(blacklists);
        return Result.success("批量添加黑名单成功");
    }

    @Operation(summary = "分页查询黑名单列表（管理员功能）")
    @GetMapping("/page")
    public Result<Map<String, Object>> getBlacklistPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String plateNumber,
            @RequestParam(required = false) String reason) {

        int offset = (page - 1) * size;

        long total = blacklistService.lambdaQuery()
                .like(plateNumber != null, Blacklist::getPlateNumber, plateNumber)
                .like(reason != null, Blacklist::getReason, reason)
                .count();

        List<Blacklist> records = blacklistService.getBaseMapper().selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Blacklist>()
                        .like(plateNumber != null, "plate_number", plateNumber)
                        .like(reason != null, "reason", reason)
                        .orderByDesc("create_time")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);

        return Result.success("查询黑名单分页列表成功", response);
    }
}