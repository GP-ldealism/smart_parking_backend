package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.domain.entity.UserPlate;
import cn.gp.smartparking.service.UserPlateService;
import cn.gp.smartparking.mapper.UserPlateMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【user_plate(用户车牌表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class UserPlateServiceImpl extends ServiceImpl<UserPlateMapper, UserPlate>
    implements UserPlateService{

}




