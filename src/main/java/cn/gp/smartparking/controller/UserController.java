package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.model.entity.UserPlate;
import cn.gp.smartparking.model.entity.UserPreference;
import cn.gp.smartparking.model.vo.OrderStatisticVO;
import cn.gp.smartparking.model.vo.UserVO;
import cn.gp.smartparking.service.ParkingOrderService;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.service.UserPlateService;
import cn.gp.smartparking.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName UserController
 * @Description 用户管理控制器 - 处理用户认证、个人信息、车牌管理、偏好设置
 * @Author Guoping He
 * @Date 2026/3/24 13:55
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private ParkingOrderService parkingOrderService;

    @Resource
    private UserPlateService userPlateService;

    @Resource
    private UserPreferenceService userPreferenceService;

    // ========== 用户认证相关 ==========
    
    @Log(module = "用户管理", operation = "注册", description = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@RequestBody User user) {
        long id = userService.userRegister(user);
        return Result.success("注册成功", id);
    }

    @Log(module = "用户管理", operation = "登录", description = "用户登录")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody User user, HttpServletRequest request) {
        UserVO userVO = userService.userLogin(request, user);
        return Result.success("登录成功", userVO);
    }

    @Log(module = "用户管理", operation = "登出", description = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        userService.userLogout(request);
        return Result.success("退出登录成功");
    }

    // ========== 用户个人信息相关 ==========
    
    @Log(module = "用户管理", operation = "更新", description = "更新用户信息")
    @PutMapping("/update/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        userService.updateUserProfile(id, user);
        return Result.success("修改成功");
    }

    @Operation(summary = "获取用户订单统计信息")
    @GetMapping("/statistics/{id}")
    public Result<OrderStatisticVO> getUserOrderStatistics(@PathVariable Long id) {
        OrderStatisticVO statistics = parkingOrderService.getUserOrderStatistics(id);
        return Result.success("获取用户订单统计信息成功", statistics);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/{id}")
    public Result<User> getUserInfo(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success("获取用户信息成功", user);
    }

    // ========== 用户车牌管理相关 ==========
    
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
                .eq(cn.gp.smartparking.model.entity.ParkingOrder::getUserId, userId)
                .eq(cn.gp.smartparking.model.entity.ParkingOrder::getStatus, 0)
                .isNotNull(cn.gp.smartparking.model.entity.ParkingOrder::getPlateNumber)
                .list()
                .stream()
                .map(cn.gp.smartparking.model.entity.ParkingOrder::getPlateNumber)
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

    // ========== 用户偏好设置相关 ==========
    
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

}
