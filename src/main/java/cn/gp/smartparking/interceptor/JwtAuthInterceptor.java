package cn.gp.smartparking.interceptor;

import cn.gp.smartparking.common.JwtUtils;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.RedisTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisTokenService redisTokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40100, \"message\": \"未授权，请登录\"}");
            return false;
        }

        String token = authorization.substring(7);

        if (!JwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40100, \"message\": \"token无效或已过期\"}");
            return false;
        }

        Long userId = JwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40100, \"message\": \"用户不存在\"}");
            return false;
        }

        if (!redisTokenService.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40100, \"message\": \"token已失效，请重新登录\"}");
            return false;
        }

        User user = redisTokenService.getUserByToken(token);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40100, \"message\": \"用户信息获取失败\"}");
            return false;
        }

        if (user.getStatus() == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 40100, \"message\": \"账号已被禁用\"}");
            return false;
        }

        request.setAttribute("user", user);
        request.setAttribute("userId", userId);
        request.setAttribute("token", token);

        return true;
    }
}