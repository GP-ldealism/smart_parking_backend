package cn.gp.smartparking.aspect;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.model.entity.BaseSysLog;
import cn.gp.smartparking.service.BaseSysLogService;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * 系统日志切面
 * 通过AOP拦截带有@Log注解的方法，记录操作日志
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    @Resource
    private BaseSysLogService baseSysLogService;

    /**
     * 定义切点：拦截所有带有@Log注解的方法
     */
    @Pointcut("@annotation(cn.gp.smartparking.annotation.Log)")
    public void logPointCut() {
    }

    /**
     * 环绕通知：记录方法执行前后的日志
     */
    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        
        // 将接口路径放入MDC，供SQL拦截器使用
        HttpServletRequest request = getRequest();
        if (request != null) {
            String requestUri = request.getRequestURI();
            String requestMethod = request.getMethod();
            MDC.put("requestUri", requestUri);
            MDC.put("requestMethod", requestMethod);
        }
        
        try {
            // 执行目标方法
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executeTime = endTime - beginTime;
            
            // 记录日志，传入result以便从返回值中获取userId
            saveLog(point, exception, executeTime, result);
            
            // 清理MDC
            MDC.remove("requestUri");
            MDC.remove("requestMethod");
        }
    }

    /**
     * 保存日志到数据库
     */
    private void saveLog(ProceedingJoinPoint joinPoint, Exception exception, long executeTime, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log logAnnotation = method.getAnnotation(Log.class);
            
            if (logAnnotation == null) {
                return;
            }
            
            BaseSysLog sysLog = new BaseSysLog();
            
            // 设置操作模块和类型
            sysLog.setModule(logAnnotation.module());
            sysLog.setOperation(logAnnotation.operation());
            
            // 设置操作内容
            String description = logAnnotation.description();
            if (StrUtil.isBlank(description)) {
                description = method.getName();
            }
            sysLog.setContent(description + "，耗时：" + executeTime + "ms");
            
            // 设置IP地址
            HttpServletRequest request = getRequest();
            Long userId = null;
            if (request != null) {
                String ip = getClientIp(request);
                sysLog.setIp(ip);
                
                // 设置用户ID（从request attribute中获取，由JWT拦截器设置）
                Object userIdObj = request.getAttribute("userId");
                if (userIdObj != null) {
                    userId = Long.valueOf(userIdObj.toString());
                } else {
                    // 如果request中没有userId，尝试从方法参数中获取（登录、注册等场景）
                    Object[] args = joinPoint.getArgs();
                    for (Object arg : args) {
                        if (arg instanceof cn.gp.smartparking.model.entity.User) {
                            cn.gp.smartparking.model.entity.User user = (cn.gp.smartparking.model.entity.User) arg;
                            if (user.getId() != null) {
                                userId = user.getId();
                                break;
                            }
                        }
                    }
                    
                    // 如果参数中也没有userId，尝试从返回值中获取（登录场景）
                    if (userId == null && result != null) {
                        if (result instanceof cn.gp.smartparking.common.Result) {
                            Object data = ((cn.gp.smartparking.common.Result) result).getData();
                            if (data instanceof cn.gp.smartparking.model.vo.UserVO) {
                                cn.gp.smartparking.model.vo.UserVO userVO = (cn.gp.smartparking.model.vo.UserVO) data;
                                if (userVO.getId() != null) {
                                    userId = userVO.getId();
                                }
                            } else if (data instanceof Long) {
                                userId = (Long) data;
                            }
                        }
                    }
                }
            }
            
            // 设置用户ID
            sysLog.setUserId(userId);
            
            // 设置执行状态
            if (exception != null) {
                sysLog.setStatus(0); // 失败
                sysLog.setErrorMsg(exception.getMessage());
            } else {
                sysLog.setStatus(1); // 成功
            }
            
            // 设置创建时间
            sysLog.setCreateTime(new Date());
            
            // 保存到数据库
            baseSysLogService.save(sysLog);
            
        } catch (Exception e) {
            log.error("保存系统日志失败", e);
        }
    }

    /**
     * 获取当前请求对象
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多级代理的情况，取第一个IP
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
