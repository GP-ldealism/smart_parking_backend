package cn.gp.smartparking.service.impl;

import cn.gp.smartparking.common.BusinessCode;
import cn.gp.smartparking.common.JwtUtils;
import cn.gp.smartparking.common.PasswordUtils;
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

        // 2. MD5加密密码
        String encryptedPassword = PasswordUtils.encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);

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

        // 2. 查询用户信息（根据用户名）
        User existingUser = lambdaQuery().eq(User::getUsername, username).one();
        if (existingUser == null) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户不存在");
        }

        // 3. 验证密码（MD5加密后比对）
        if (!PasswordUtils.verifyPassword(password, existingUser.getPassword())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "密码错误");
        }

        // 4. 检查用户状态
        if (existingUser.getStatus() == 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "账号已被禁用");
        }

        // 5. 生成JWT Token
        String token = JwtUtils.generateToken(existingUser.getId(), existingUser.getUsername(), existingUser.getRole());

        // 6. 构建返回的用户信息
        UserVO userVO = new UserVO();
        userVO.setId(existingUser.getId());
        userVO.setUsername(existingUser.getUsername());
        userVO.setNickname(existingUser.getNickname());
        userVO.setPhone(existingUser.getPhone());
        userVO.setRole(existingUser.getRole());
        userVO.setCreate_time(existingUser.getCreateTime());
        userVO.setToken(token);

        // 7. 保存用户登录态到session
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, existingUser);

        return userVO;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
    }

    @Override
    public boolean updateUserProfile(Long id, User user) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户ID不能为空");
        }

        if (user.getPassword() != null) {
            user.setPassword(PasswordUtils.encryptPassword(user.getPassword()));
        }
        // 2. 执行更新
        int rows = userMapper.chooseUpdateById(id, user);

        // 3. 根据返回值判断结果
        if (rows <= 0) {
            log.debug("更新失败，未找到用户: {}", user.getId());
            return false;
        }
        return true;
    }

    @Override
    public Long createUser(User user) {
        // 1. 参数校验
        if (StrUtil.hasBlank(user.getUsername())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户名为空");
        }

        // 2. 检查用户名是否已存在
        int userCnt = userMapper.selectByUsername(user.getUsername());
        if (userCnt > 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户名已存在");
        }

        // 3. 如果没有提供密码，设置默认密码
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword("12345678"); // 默认密码
        }

        // 4. MD5加密密码
        String encryptedPassword = PasswordUtils.encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);

        // 5. 设置默认值
        if (user.getRole() == null) {
            user.setRole(0); // 默认普通用户（车主）
        }
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认正常状态
        }
        if (user.getNickname() == null) {
            user.setNickname(user.getUsername()); // 默认昵称为用户名
        }

        // 6. 保存用户
        userMapper.insert(user);

        return user.getId();
    }
}




