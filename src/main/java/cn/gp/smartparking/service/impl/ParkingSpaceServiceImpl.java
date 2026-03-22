package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingSpaceService;
import cn.gp.smartparking.mapper.ParkingSpaceMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【parking_space(车位信息表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class ParkingSpaceServiceImpl extends ServiceImpl<ParkingSpaceMapper, ParkingSpace>
    implements ParkingSpaceService{

}




