package cn.gp.smartparking.service;

import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author HeGuoping
* @description 针对表【user(系统用户表)】的数据库操作Service
* @createDate 2026-03-18 22:44:40
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param user
     * @return
     */
    Long userRegister(User user);

    /**
     * 用户登录
     * @param request
     * @param user
     * @return
     */
    UserVO userLogin(HttpServletRequest request, User user);

    /**
     * 用户退出登录
     * @param request
     * @return
     */
    void userLogout(HttpServletRequest request);

    boolean updateUserProfile(Long id, User user);

    /**
     * 创建用户（管理员功能）
     * @param user 用户信息
     * @return 新用户ID
     */
    Long createUser(User user);
}
