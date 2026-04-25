package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.model.vo.OrderStatisticVO;
import cn.gp.smartparking.model.vo.UserVO;
import cn.gp.smartparking.service.ParkingOrderService;
import cn.gp.smartparking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @ClassName UserController
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/3/24 13:55
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private ParkingOrderService parkingOrderService;

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

    @Log(module = "用户管理", operation = "更新", description = "更新用户信息")
    @PutMapping("/update/{id}")
    public Result<String> update(@PathVariable Long id,
                                 @RequestBody User user) {
        userService.updateUserProfile(id, user);
        return Result.success("修改成功");
    }

    @Operation(summary = "获取用户订单统计信息")
    @GetMapping("/statistics/{id}")
    public Result<OrderStatisticVO> getUserOrderStatistics(@PathVariable Long id) {
        OrderStatisticVO statistics = parkingOrderService.getUserOrderStatistics(id);
        return Result.success("获取用户订单统计信息成功", statistics);
    }

}
