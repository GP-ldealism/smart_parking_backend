package cn.gp.smartparking.interceptor;

import cn.gp.smartparking.common.JwtUtils;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Authorization头
        String authorization = request.getHeader("Authorization");
        
        // 检查token是否存在
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 40100, \"message\": \"未授权，请登录\"}");
            return false;
        }
        
        // 提取token
        String token = authorization.substring(7);
        
        // 验证token
        if (!JwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 40100, \"message\": \"token无效或已过期\"}");
            return false;
        }
        
        // 获取用户信息
        Long userId = JwtUtils.getUserIdFromToken(token);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 40100, \"message\": \"用户不存在\"}");
            return false;
        }
        
        // 查询用户信息
        User user = userService.getById(userId);
        if (user == null || user.getStatus() == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\": 40100, \"message\": \"用户不存在或已禁用\"}");
            return false;
        }
        
        // 将用户信息存入session
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        
        return true;
    }
}