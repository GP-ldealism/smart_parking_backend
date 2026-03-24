package cn.gp.smartparking.service.impl;

import cn.gp.smartparking.common.BusinessCode;
import cn.gp.smartparking.constant.UserConstant;
import cn.gp.smartparking.exception.BusinessException;
import cn.gp.smartparking.model.vo.UserVO;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【user(系统用户表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Override
    public Long userRegister(User user) {
        // 1. 检验参数
        if (StrUtil.hasBlank(user.getUsername(), user.getPassword())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "参数为空");
        }
        int userCnt = userMapper.selectByUsername(user.getUsername());
        if (userCnt > 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户名重复");
        }

        int row = userMapper.userRegister(user);
        if (row > 0) {
            Long id = user.getId();
            if (id > 0) {
                return id;
            }
        }
        return null;
    }

    @Override
    public UserVO userLogin(HttpServletRequest request, User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        // 1. 检验参数
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "参数为空");
        }

        // 2. 加密密码
//        String encryptPassword = getEncryptedPassword(userPassword);
        // 3. 检查用户账号是否存在
        UserVO userVO = userMapper.selectByUsernameAndPassword(username, password);
        if (userVO == null) {
            log.info("user login failed, username cannot match password");
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 4. 保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);

        return userVO;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
    }
}




