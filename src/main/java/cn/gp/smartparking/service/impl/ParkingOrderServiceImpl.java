package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.service.ParkingOrderService;
import cn.gp.smartparking.mapper.ParkingOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【parking_order(停车订单表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class ParkingOrderServiceImpl extends ServiceImpl<ParkingOrderMapper, ParkingOrder>
    implements ParkingOrderService{

}




