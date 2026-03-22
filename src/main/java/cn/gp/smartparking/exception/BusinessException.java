package cn.gp.smartparking.exception;

import cn.gp.smartparking.common.BusinessCode;
import lombok.Getter;

/**
 * @ClassName BusinessException
 * @Description 自定义业务异常
 * @Author Guoping He
 * @Date 2026/3/22 11:49
 */
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 业务码
     */
    private final int code;

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(BusinessCode businessCode) {
        super(businessCode.getMessage());
        this.code = businessCode.getCode();
    }

    public BusinessException(BusinessCode businessCode, String msg) {
        super(msg);
        this.code = businessCode.getCode();
    }
}
