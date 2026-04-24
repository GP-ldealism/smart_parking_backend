package cn.gp.smartparking.service.impl;

import cn.gp.smartparking.common.BusinessCode;
import cn.gp.smartparking.common.JwtUtils;
import cn.gp.smartparking.common.PasswordUtils;
import cn.gp.smartparking.exception.BusinessException;
import cn.gp.smartparking.model.vo.UserVO;
import cn.gp.smartparking.service.RedisTokenService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTokenService redisTokenService;

    @Override
    public Long userRegister(User user) {
        if (StrUtil.hasBlank(user.getUsername(), user.getPassword())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "参数为空");
        }
        int userCnt = userMapper.selectByUsername(user.getUsername());
        if (userCnt > 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户名重复");
        }

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
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "参数为空");
        }

        User existingUser = lambdaQuery().eq(User::getUsername, username).one();
        if (existingUser == null) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户不存在");
        }

        if (!PasswordUtils.verifyPassword(password, existingUser.getPassword())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "密码错误");
        }

        if (existingUser.getStatus() == 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "账号已被禁用");
        }

        String token = JwtUtils.generateToken(existingUser.getId(), existingUser.getUsername(), existingUser.getRole());

        redisTokenService.saveToken(token, existingUser);

        UserVO userVO = new UserVO();
        userVO.setId(existingUser.getId());
        userVO.setUsername(existingUser.getUsername());
        userVO.setNickname(existingUser.getNickname());
        userVO.setPhone(existingUser.getPhone());
        userVO.setRole(existingUser.getRole());
        userVO.setCreate_time(existingUser.getCreateTime());
        userVO.setToken(token);

        return userVO;
    }

    @Override
    public void userLogout(HttpServletRequest request) {
        Object tokenAttr = request.getAttribute("token");
        if (tokenAttr != null) {
            redisTokenService.removeToken(tokenAttr.toString());
        }
    }

    @Override
    public boolean updateUserProfile(Long id, User user) {
        if (id == null || id <= 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户ID不能为空");
        }

        if (user.getPassword() != null) {
            user.setPassword(PasswordUtils.encryptPassword(user.getPassword()));
        }
        int rows = userMapper.chooseUpdateById(id, user);

        if (rows <= 0) {
            log.debug("更新失败，未找到用户: {}", user.getId());
            return false;
        }

        if (user.getPassword() != null) {
            redisTokenService.removeTokenByUserId(id);
        }

        return true;
    }

    @Override
    public Long createUser(User user) {
        if (StrUtil.hasBlank(user.getUsername())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户名为空");
        }

        int userCnt = userMapper.selectByUsername(user.getUsername());
        if (userCnt > 0) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR, "用户名已存在");
        }

        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword("12345678");
        }

        String encryptedPassword = PasswordUtils.encryptPassword(user.getPassword());
        user.setPassword(encryptedPassword);

        if (user.getRole() == null) {
            user.setRole(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getNickname() == null) {
            user.setNickname(user.getUsername());
        }

        userMapper.insert(user);

        return user.getId();
    }
}