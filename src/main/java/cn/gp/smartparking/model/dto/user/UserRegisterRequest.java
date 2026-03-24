package cn.gp.smartparking.model.dto.user;

import lombok.Data;

/**
 * @ClassName UserRegisterRequest
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/3/24 14:42
 */
@Data
public class UserRegisterRequest {
    /**
     * 账号
     */
    private String username;

    /**
     * 密码（加密）
     */
    private String password;

    /**
     * 手机号
     */
    private String phone;
}
