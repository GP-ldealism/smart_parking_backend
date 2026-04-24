package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.Notification;
import cn.gp.smartparking.service.NotificationService;
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
@RequestMapping("/notification")
@Tag(name = "消息通知管理")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Operation(summary = "获取用户通知列表")
    @GetMapping("/user/{userId}")
    public Result<List<Notification>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(required = false) Byte isRead) {
        
        List<Notification> notifications = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(isRead != null, Notification::getIsRead, isRead)
                .orderByDesc(Notification::getCreateTime)
                .list();
        
        return Result.success("获取用户通知列表成功", notifications);
    }

    @Operation(summary = "获取通知详情")
    @GetMapping("/{id}")
    public Result<Notification> getNotificationDetail(@PathVariable Long id) {
        Notification notification = notificationService.getById(id);
        // 标记为已读
        if (notification != null && notification.getIsRead() == 0) {
            notification.setIsRead(1);
            notificationService.updateById(notification);
        }
        return Result.success("获取通知详情成功", notification);
    }

    @Operation(summary = "创建通知")
    @PostMapping
    public Result<Notification> createNotification(@RequestBody Notification notification) {
        notificationService.save(notification);
        return Result.success("创建通知成功", notification);
    }

    @Operation(summary = "标记通知为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.getById(id);
        if (notification != null) {
            notification.setIsRead(1);
            notificationService.updateById(notification);
            return Result.success("标记通知为已读成功");
        }
        return Result.fail("通知不存在");
    }

    @Operation(summary = "批量标记为已读")
    @PutMapping("/batch/read")
    public Result<Void> batchMarkAsRead(@RequestParam List<Long> ids) {
        notificationService.lambdaUpdate()
                .in(Notification::getId, ids)
                .set(Notification::getIsRead, (byte) 1)
                .update();
        return Result.success("批量标记为已读成功");
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.removeById(id);
        return Result.success("删除通知成功");
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/user/{userId}/unreadCount")
    public Result<Long> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead,  0)
                .count();
        return Result.success("获取未读通知数量成功", count);
    }

    @Operation(summary = "分页查询通知列表（管理员功能）")
    @GetMapping("/admin/page")
    public Result<Map<String, Object>> getNotificationPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer isRead) {

        int offset = (page - 1) * size;

        long total = notificationService.lambdaQuery()
                .eq(userId != null, Notification::getUserId, userId)
                .like(title != null, Notification::getTitle, title)
                .eq(type != null, Notification::getType, type)
                .eq(isRead != null, Notification::getIsRead, isRead)
                .count();

        List<Notification> records = notificationService.getBaseMapper().selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Notification>()
                        .eq(userId != null, "user_id", userId)
                        .like(title != null, "title", title)
                        .eq(type != null, "type", type)
                        .eq(isRead != null, "is_read", isRead)
                        .orderByDesc("create_time")
                        .last("LIMIT " + size + " OFFSET " + offset)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", total);
        response.put("current", page);
        response.put("size", size);

        return Result.success("查询通知分页列表成功", response);
    }

    @Operation(summary = "获取通知统计数据（管理员功能）")
    @GetMapping("/admin/statistics")
    public Result<Map<String, Object>> getNotificationStats() {

        long totalCount = notificationService.count();
        long unreadCount = notificationService.lambdaQuery()
                .eq(Notification::getIsRead, 0)
                .count();
        long readCount = notificationService.lambdaQuery()
                .eq(Notification::getIsRead, 1)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("unreadCount", unreadCount);
        stats.put("readCount", readCount);

        return Result.success("获取通知统计数据成功", stats);
    }
}