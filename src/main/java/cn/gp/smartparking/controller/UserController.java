package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.model.vo.UserVO;
import cn.gp.smartparking.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/register")
    public Result<Long> register(@RequestBody User user) {
        long id = userService.userRegister(user);
        return Result.success("注册成功", id);
    }

    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody User user, HttpServletRequest request) {
        UserVO userVO = userService.userLogin(request, user);
        return Result.success("登录成功", userVO);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        userService.userLogout(request);
        return Result.success("退出登录成功");
    }
}
