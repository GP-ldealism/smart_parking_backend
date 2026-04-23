package cn.gp.smartparking.mapper;

import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.model.vo.OrderStatisticVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author HeGuoping
* @description 针对表【parking_order(停车订单表)】的数据库操作Mapper
* @createDate 2026-03-18 22:44:40
* @Entity cn.gp.smartparking.domain.entity.ParkingOrder
*/
public interface ParkingOrderMapper extends BaseMapper<ParkingOrder> {
    OrderStatisticVO getUserOrderStatistics(@Param("id") Long id);
}




