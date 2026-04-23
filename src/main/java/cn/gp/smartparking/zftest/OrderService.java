package cn.gp.smartparking.zftest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName OrderService
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/4/21 21:12
 */
@Slf4j
@Service
public class OrderService {
    public OrderModel getOrder(Integer id) {
        OrderModel orderModel = new OrderModel();
        orderModel.setId(id);
        orderModel.setOrderNo("1234567890");
        orderModel.setOrderStatus(1);
        orderModel.setUserId(1);
        orderModel.setOrderPrice(new BigDecimal(100));
        orderModel.setPayType(1);
        orderModel.setPayTime(new Date());
        orderModel.setCreateTime(new Date());
        orderModel.setUpdateTime(new Date());
        return orderModel;
    }

    public boolean updateOrder(OrderModel orderModel) {
        log.debug("success update order");
        return true;
    }
}
