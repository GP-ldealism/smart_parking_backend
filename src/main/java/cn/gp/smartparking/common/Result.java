package cn.gp.smartparking.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String message;
    private T data;

    public static <T>  Result<T> success(int code, String message, T data) {
        return new Result<>(code, message, data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(BusinessCode.SUCCESS.getCode(), message, data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(BusinessCode.SUCCESS.getCode(), message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(BusinessCode.PARAMS_ERROR.getCode(), message, null);
    }

}
