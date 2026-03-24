package cn.gp.smartparking.exception;

import cn.gp.smartparking.common.BusinessCode;

/**
 * @ClassName ThrowUtils
 * @Description 异常处理工具类
 * @Author He Guoping
 * @Date 2025/12/31 23:40
 * @Version JDK17
 */
public class ThrowUtils {
    /**
     * @Param condition 条件
     * @Param runtimeException 异常
     * @Return void
     * @Description TODO
     * @Author He Guoping
     * @Date 2025/12/31 23:44
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }
    /**
     * @Param condition 条件
     * @Param errorCode 状态码
     * @Return void
     * @Description TODO
     * @Author He Guoping
     * @Date 2025/12/31 23:45
     */
    public static void throwIf(boolean condition, BusinessCode businessCode) {
        throwIf(condition, new BusinessException(businessCode));

    }
    /**
     * @Param condition 条件
     * @Param errorCode 状态码
     * @Param message 错误信息
     * @Return void
     * @Description TODO
     * @Author He Guoping
     * @Date 2025/12/31 23:45
     */
    public static void throwIf(boolean condition, BusinessCode businessCode, String message) {
        throwIf(condition, new BusinessException(businessCode, message));
    }
}
