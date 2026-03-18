package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.domain.entity.ParkingLot;
import cn.gp.smartparking.service.ParkingLotService;
import cn.gp.smartparking.mapper.ParkingLotMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【parking_lot(停车场信息表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot>
    implements ParkingLotService{

}




