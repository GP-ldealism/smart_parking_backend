package cn.gp.smartparking.constant;

/**
 * @ClassName UserConstant
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/3/24 15:47
 */
public interface UserConstant {
    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login_status";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";
    /**
     * 加密SALT
     */
    String SALT = "smartparking";
}
