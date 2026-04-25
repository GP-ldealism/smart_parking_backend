package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.model.entity.UserPlate;
import cn.gp.smartparking.model.entity.UserPreference;
import cn.gp.smartparking.service.ParkingOrderService;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.service.UserPlateService;
import cn.gp.smartparking.service.UserPreferenceService;
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
@RequestMapping("/userManagement")
@Tag(name = "用户管理(管理员)")
public class UserManagementController {

    @Resource
    private UserService userService;

    @Resource
    private UserPlateService userPlateService;

    @Resource
    private UserPreferenceService userPreferenceService;

    @Resource
    private ParkingOrderService parkingOrderService;

    @Operation(summary = "获取用户信息")
    @GetMapping("/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        User user = userService.getById(userId);
        return Result.success("获取用户信息成功", user);
    }

    @Log(module = "用户管理", operation = "更新信息", description = "更新用户信息")
    @Operation(summary = "更新用户信息")
    @PutMapping("/{userId}")
    public Result<User> updateUserInfo(@PathVariable Long userId, @RequestBody User user) {
        user.setId(userId);
        userService.updateById(user);
        User updatedUser = userService.getById(userId);
        return Result.success("更新用户信息成功", updatedUser);
    }

    @Operation(summary = "获取用户车牌列表")
    @GetMapping("/{userId}/plates")
    public Result<List<UserPlate>> getUserPlates(@PathVariable Long userId) {
        List<UserPlate> plates = userPlateService.lambdaQuery()
                .eq(UserPlate::getUserId, userId)
                .eq(UserPlate::getStatus, 1) // 只查询正常状态的车牌
                .list();
        return Result.success("获取用户车牌列表成功", plates);
    }

    @Operation(summary = "获取用户可用车牌列表（排除已预约的车辆）")
    @GetMapping("/{userId}/plates/available")
    public Result<List<UserPlate>> getAvailablePlates(@PathVariable Long userId) {
        // 获取用户所有正常状态的车牌
        List<UserPlate> allPlates = userPlateService.lambdaQuery()
                .eq(UserPlate::getUserId, userId)
                .eq(UserPlate::getStatus, 1)
                .list();
        
        // 获取用户所有进行中的订单（status=0）
        List<String> usedPlateNumbers = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getUserId, userId)
                .eq(ParkingOrder::getStatus, 0)
                .isNotNull(ParkingOrder::getPlateNumber)
                .list()
                .stream()
                .map(ParkingOrder::getPlateNumber)
                .toList();
        
        // 过滤出可用车牌
        List<UserPlate> availablePlates = allPlates.stream()
                .filter(plate -> !usedPlateNumbers.contains(plate.getPlateNumber()))
                .toList();
        
        return Result.success("获取用户可用车牌列表成功", availablePlates);
    }

    @Log(module = "用户管理", operation = "添加车牌", description = "添加用户车牌")
    @Operation(summary = "添加用户车牌")
    @PostMapping("/{userId}/plates")
    public Result<UserPlate> addUserPlate(@PathVariable Long userId, @RequestBody UserPlate plate) {
        plate.setUserId(userId);
        userPlateService.save(plate);
        return Result.success("添加用户车牌成功", plate);
    }

    @Log(module = "用户管理", operation = "删除车牌", description = "删除用户车牌")
    @Operation(summary = "删除用户车牌")
    @DeleteMapping("/plates/{plateId}")
    public Result<Void> deleteUserPlate(@PathVariable Long plateId) {
        userPlateService.removeById(plateId);
        return Result.success("删除用户车牌成功");
    }

    @Log(module = "用户管理", operation = "设置默认车牌", description = "设置默认车牌")
    @Operation(summary = "设置默认车牌")
    @PutMapping("/plates/{plateId}/default")
    public Result<Void> setDefaultPlate(@PathVariable Long plateId) {
        UserPlate plate = userPlateService.getById(plateId);
        if (plate != null) {
            // 先将该用户的所有车牌设置为非默认
            userPlateService.lambdaUpdate()
                    .eq(UserPlate::getUserId, plate.getUserId())
                    .set(UserPlate::getIsDefault, (byte) 0)
                    .update();
            
            // 再设置当前车牌为默认
            plate.setIsDefault((int) 1);
            userPlateService.updateById(plate);
            
            return Result.success("设置默认车牌成功");
        }
        return Result.fail("车牌不存在");
    }

    @Operation(summary = "获取用户偏好设置")
    @GetMapping("/{userId}/preference")
    public Result<UserPreference> getUserPreference(@PathVariable Long userId) {
        UserPreference preference = userPreferenceService.lambdaQuery()
                .eq(UserPreference::getUserId, userId)
                .one();
        
        // 如果用户偏好不存在，返回默认偏好
        if (preference == null) {
            preference = new UserPreference();
            preference.setUserId(userId);
            preference.setPreferDistance(1000); // 默认1000米
            preference.setPreferPrice(0); // 默认便宜优先
            preference.setPreferType((int) 0); // 默认普通车位
            userPreferenceService.save(preference);
        }
        
        return Result.success("获取用户偏好设置成功", preference);
    }

    @Log(module = "用户管理", operation = "更新偏好", description = "更新用户偏好设置")
    @Operation(summary = "更新用户偏好设置")
    @PutMapping("/{userId}/preference")
    public Result<UserPreference> updateUserPreference(
            @PathVariable Long userId, 
            @RequestBody UserPreference preference) {
        
        UserPreference existing = userPreferenceService.lambdaQuery()
                .eq(UserPreference::getUserId, userId)
                .one();
        
        if (existing != null) {
            preference.setId(existing.getId());
            preference.setUserId(userId);
            userPreferenceService.updateById(preference);
        } else {
            preference.setUserId(userId);
            userPreferenceService.save(preference);
        }
        
        return Result.success("更新用户偏好设置成功", preference);
    }

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
            @RequestParam Integer status) {
        
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
}