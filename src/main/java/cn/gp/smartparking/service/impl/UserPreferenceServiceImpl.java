package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.domain.entity.UserPreference;
import cn.gp.smartparking.service.UserPreferenceService;
import cn.gp.smartparking.mapper.UserPreferenceMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【user_preference(用户停车偏好表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference>
    implements UserPreferenceService{

}




