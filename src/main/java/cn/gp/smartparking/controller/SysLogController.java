package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.BaseSysLog;
import cn.gp.smartparking.service.BaseSysLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sysLog")
@Tag(name = "系统日志管理")
public class SysLogController {

    @Resource
    private BaseSysLogService sysLogService;

    @Operation(summary = "获取日志列表")
    @GetMapping("/list")
    public Result<List<BaseSysLog>> getLogList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        List<BaseSysLog> logs = sysLogService.lambdaQuery()
                .eq(userId != null, BaseSysLog::getUserId, userId)
                .orderByDesc(BaseSysLog::getCreateTime)
                .list();
        
        return Result.success("获取日志列表成功", logs);
    }

    @Operation(summary = "获取日志详情")
    @GetMapping("/{id}")
    public Result<BaseSysLog> getLogDetail(@PathVariable Long id) {
        BaseSysLog log = sysLogService.getById(id);
        return Result.success("获取日志详情成功", log);
    }

    @Operation(summary = "记录操作日志")
    @PostMapping
    public Result<BaseSysLog> createLog(@RequestBody BaseSysLog log) {
        sysLogService.save(log);
        return Result.success("记录操作日志成功", log);
    }

    @Operation(summary = "获取用户操作日志")
    @GetMapping("/user/{userId}")
    public Result<List<BaseSysLog>> getUserLogs(@PathVariable Long userId) {
        List<BaseSysLog> logs = sysLogService.lambdaQuery()
                .eq(BaseSysLog::getUserId, userId)
                .orderByDesc(BaseSysLog::getCreateTime)
                .list();
        return Result.success("获取用户操作日志成功", logs);
    }

    @Operation(summary = "获取日志统计")
    @GetMapping("/statistics")
    public Result<Object> getLogStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        // 统计功能可以根据实际需求实现
        Object statistics = new Object();
        return Result.success("获取日志统计成功", statistics);
    }

    @Operation(summary = "分页查询日志列表（管理员功能）")
    @GetMapping("/page")
    public Result<Map<String, Object>> getLogPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询总数
        long total = sysLogService.lambdaQuery()
                .eq(userId != null, BaseSysLog::getUserId, userId)
                .like(content != null, BaseSysLog::getContent, content)
                .count();

        // 查询日志列表（按时间倒序）
        List<BaseSysLog> records = sysLogService.getBaseMapper().selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BaseSysLog>()
                        .eq(userId != null, "user_id", userId)
                        .like(content != null, "content", content)
                        .orderByDesc("create_time")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );

        // 构建返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);

        return Result.success("查询日志分页列表成功", response);
    }
}