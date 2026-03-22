package cn.gp.smartparking.exception;

import cn.gp.smartparking.common.BusinessCode;
import cn.gp.smartparking.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName GlobalExceptionHandler
 * @Description 全局异常处理
 * @Author Guoping He
 * @Date 2026/3/22 11:56
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException ----------", e);
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException  ----------", e);
        return Result.fail(BusinessCode.SYSTEM_ERROR.getCode(), BusinessCode.SYSTEM_ERROR.getMessage());
    }
}
