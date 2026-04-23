package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingOrderService;
import cn.gp.smartparking.service.ParkingSpaceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/parkingOrder")
@Tag(name = "停车订单管理")
public class ParkingOrderController {

    @Resource
    private ParkingOrderService parkingOrderService;

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Operation(summary = "创建停车订单")
    @PostMapping("create")
    @Transactional
    public Result<ParkingOrder> createOrder(@RequestBody ParkingOrder order) {
        if (order.getUserId() == null) {
            return Result.fail("用户ID不能为空");
        }
        if (order.getParkingLotId() == null) {
            return Result.fail("停车场ID不能为空");
        }
        if (order.getType() == null) {
            order.setType(0);
//            return Result.fail("订单类型不能为空");
        }

        QueryWrapper<ParkingSpace> parkingSpaceQueryWrapper = new QueryWrapper<>();
        parkingSpaceQueryWrapper.lambda()
                .eq(ParkingSpace::getParkingLotId, order.getParkingLotId())
                .eq(ParkingSpace::getSpaceNo, order.getSpaceNo())
                .eq(ParkingSpace::getStatus, 1);

        ParkingSpace one = parkingSpaceService.getOne(parkingSpaceQueryWrapper);
        if (one == null) {
            return Result.fail("该车位暂不可用，请选择其他车位!");
        }

        order.setOrderNo(generateOrderNo());
        order.setStatus(0);
        order.setCreateTime(new Date());

        parkingOrderService.save(order);
        one.setStatus(0);
        parkingSpaceService.updateById(one);
        return Result.success("创建订单成功", order);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PK" + timestamp + uuid;
    }

    @Operation(summary = "获取订单详情")
    @GetMapping("/{id}")
    public Result<ParkingOrder> getOrderDetail(@PathVariable Long id) {
        ParkingOrder order = parkingOrderService.getById(id);
        return Result.success("获取订单详情成功", order);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/user")
    public Result<List<ParkingOrder>> getUserOrders(@RequestParam Long userId) {
        List<ParkingOrder> orders = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getUserId, userId)
                .orderByDesc(ParkingOrder::getCreateTime)
                .list();
        return Result.success("获取用户订单列表成功", orders);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        ParkingOrder order = parkingOrderService.getById(id);
        if (order != null && order.getStatus() == 0) { // 只有待进场状态可以取消
            order.setStatus((int) 2); // 2表示已取消
            parkingOrderService.updateById(order);
            return Result.success("订单取消成功");
        }
        return Result.fail("订单状态不允许取消");
    }

    @Operation(summary = "完成订单")
    @PostMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        ParkingOrder order = parkingOrderService.getById(id);
        if (order != null && order.getStatus() == 0) { // 只有待进场状态可以完成
            order.setStatus((int) 1); // 1表示已完成
            parkingOrderService.updateById(order);
            return Result.success("订单完成成功");
        }
        return Result.fail("订单状态不允许完成");
    }

    @Operation(summary = "获取停车场订单列表")
    @GetMapping("/parkingLot/{parkingLotId}")
    public Result<List<ParkingOrder>> getParkingLotOrders(@PathVariable Long parkingLotId) {
        List<ParkingOrder> orders = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getParkingLotId, parkingLotId)
                .orderByDesc(ParkingOrder::getCreateTime)
                .list();
        return Result.success("获取停车场订单列表成功", orders);
    }

}