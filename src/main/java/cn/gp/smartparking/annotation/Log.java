package cn.gp.smartparking.annotation;

import java.lang.annotation.*;

/**
 * 系统日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    
    /**
     * 操作模块
     */
    String module() default "";
    
    /**
     * 操作类型
     */
    String operation() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
}
