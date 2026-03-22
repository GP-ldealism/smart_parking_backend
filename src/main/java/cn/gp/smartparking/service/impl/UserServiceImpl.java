package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【user(系统用户表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




