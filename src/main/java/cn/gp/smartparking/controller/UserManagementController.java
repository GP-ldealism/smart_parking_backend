package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName UserManagementController
 * @Description 管理员用户管理控制器 - 仅包含管理员专用功能
 * @Author Guoping He
 * @Date 2026/3/24 13:55
 */
@RestController
@RequestMapping("/admin/user")
@Tag(name = "管理员用户管理")
public class UserManagementController {

    @Resource
    private UserService userService;

    @Operation(summary = "查询用户列表（管理员功能）")
    @GetMapping("/list")
    public Result<List<User>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Byte role) {
        
        List<User> users = userService.lambdaQuery()
                .like(username != null, User::getUsername, username)
                .eq(role != null, User::getRole, role)
                .eq(User::getStatus, 1) // 只查询正常状态的用户
                .list();
        
        return Result.success("查询用户列表成功", users);
    }

    @Log(module = "用户管理", operation = "创建用户", description = "创建用户")
    @Operation(summary = "创建用户（管理员功能）")
    @PostMapping
    public Result<Long> createUser(@RequestBody User user) {
        Long userId = userService.createUser(user);
        return Result.success("创建用户成功", userId);
    }

    @Log(module = "用户管理", operation = "删除用户", description = "删除用户")
    @Operation(summary = "删除用户（软删除，管理员功能）")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        boolean success = userService.removeById(userId);
        if (success) {
            return Result.success("删除用户成功");
        }
        return Result.fail("删除用户失败");
    }

    @Log(module = "用户管理", operation = "更新状态", description = "更新用户状态")
    @Operation(summary = "更新用户状态（启用/禁用，管理员功能）")
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> requestBody) {
        
        Integer status = requestBody.get("status");
        if (status == null) {
            return Result.fail("状态参数不能为空");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        
        user.setStatus(status);
        userService.updateById(user);
        return Result.success("更新用户状态成功");
    }

    @Operation(summary = "分页查询用户列表（管理员功能）")
    @GetMapping("/page")
    public Result<Map<String, Object>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Byte role) {

        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询总数
        long total = userService.lambdaQuery()
                .like(username != null, User::getUsername, username)
                .eq(role != null, User::getRole, role)
                .count();

        // 查询用户列表（排除已删除的）
        List<User> records = userService.getBaseMapper().selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .like("username", username != null ? username : "")
                        .eq(role != null, "role", role)
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

        return Result.success("查询用户分页列表成功", response);
    }

    // ========== 兼容性接口 - 保持向后兼容（已废弃，建议使用 /user/* 接口） ==========
    
    /**
     * @deprecated 请使用 GET /user/{userId} 替代
     */
    @Deprecated
    @Operation(summary = "获取用户信息（已废弃，请使用 /user/{userId}）")
    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return Result.success("获取用户信息成功", user);
    }

    /**
     * @deprecated 请使用 PUT /user/update/{userId} 替代
     */
    @Deprecated
    @Log(module = "用户管理", operation = "更新信息", description = "更新用户信息")
    @Operation(summary = "更新用户信息（已废弃，请使用 /user/update/{userId}）")
    @PutMapping("/{userId}")
    public Result<User> updateUserInfo(@PathVariable Long userId, @RequestBody User user) {
        user.setId(userId);
        userService.updateById(user);
        User updatedUser = userService.getById(userId);
        return Result.success("更新用户信息成功", updatedUser);
    }

}