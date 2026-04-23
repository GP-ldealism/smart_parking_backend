package cn.gp.smartparking.service;

import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.model.vo.OrderStatisticVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
* @author HeGuoping
* @description 针对表【parking_order(停车订单表)】的数据库操作Service
* @createDate 2026-03-18 22:44:40
*/
public interface ParkingOrderService extends IService<ParkingOrder> {

    OrderStatisticVO getUserOrderStatistics(Long id);
}
