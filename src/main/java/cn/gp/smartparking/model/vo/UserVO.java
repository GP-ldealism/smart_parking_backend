package cn.gp.smartparking.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName UserVO
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/3/24 14:03
 */
@Data
public class UserVO implements Serializable {
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 账号
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 0=车主 1=管理员
     */
    private Integer role;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * JWT Token
     */
    private String token;

    private static final long serialVersionUID = 1L;
}
