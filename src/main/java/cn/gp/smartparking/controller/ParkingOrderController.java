package cn.gp.smartparking.controller;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.config.AlipayProperties;
import cn.gp.smartparking.model.dto.ParkingRecordDTO;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingLotService;
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
import java.util.ArrayList;
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

    @Resource
    private ParkingLotService parkingLotService;

    @Resource
    private cn.gp.smartparking.service.ParkingLotService parkingLotEntityService;

    @Resource
    private AlipayProperties alipayProperties;

    @Log(module = "订单管理", operation = "创建", description = "创建停车订单")
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
        
        // 设置开始时间为当前时间（预约时间）
        Date now = new Date();
        order.setStartTime(now);
        // 结束时间暂不设置，停车完成后设置

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
        if (order != null) {
            // 如果 startTime 为 null，使用 createTime
            if (order.getStartTime() == null && order.getCreateTime() != null) {
                order.setStartTime(order.getCreateTime());
            }
            
            // 动态计算 durationMinutes
            // 如果 endTime 有值（已完成或已取消），使用 endTime 计算
            // 如果 endTime 为 null（进行中），使用当前时间计算
            if (order.getStartTime() != null) {
                Date now = new Date();
                long endTimeMillis = (order.getEndTime() != null) 
                    ? order.getEndTime().getTime() 
                    : now.getTime();
                long durationMillis = endTimeMillis - order.getStartTime().getTime();
                int durationMinutes = (int) (durationMillis / (60 * 1000));
                order.setDurationMinutes(durationMinutes);
                
                // 如果订单进行中（status=0且endTime为null），计算当前费用（只返回不更新数据库）
                if (order.getStatus() == 0 && order.getEndTime() == null) {
                    double amount = calculateAmount(order.getStartTime(), now, order.getParkingLotId());
                    order.setAmount(java.math.BigDecimal.valueOf(amount));
                }
            }
        }
        return Result.success("获取订单详情成功", order);
    }

    @Operation(summary = "获取用户订单列表")
    @GetMapping("/user")
    public Result<List<ParkingOrder>> getUserOrders(
            @RequestParam Long userId,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder) {
        
        // 构建查询条件
        var queryWrapper = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getUserId, userId);
        
        // 处理排序
        if (sortField != null && !sortField.isEmpty()) {
            // 将驼峰命名转换为下划线命名
            String dbField = camelToUnderscore(sortField);
            String orderSql = "ascend".equals(sortOrder) ? "ASC" : "DESC";
            queryWrapper.last("ORDER BY " + dbField + " " + orderSql);
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(ParkingOrder::getCreateTime);
        }
        
        List<ParkingOrder> orders = queryWrapper.list();
        
        // 处理订单数据
        Date now = new Date();
        for (ParkingOrder order : orders) {
            // 如果 startTime 为 null，使用 createTime
            if (order.getStartTime() == null && order.getCreateTime() != null) {
                order.setStartTime(order.getCreateTime());
            }
            
            // 设置 createBy 为 userId
            if (order.getCreateBy() == null) {
                order.setCreateBy(userId);
            }
            
            // 动态计算 durationMinutes
            // 如果 endTime 有值（已完成或已取消），使用 endTime 计算
            // 如果 endTime 为 null（进行中），使用当前时间计算
            if (order.getStartTime() != null) {
                long endTimeMillis = (order.getEndTime() != null) 
                    ? order.getEndTime().getTime() 
                    : now.getTime();
                long durationMillis = endTimeMillis - order.getStartTime().getTime();
                int durationMinutes = (int) (durationMillis / (60 * 1000));
                order.setDurationMinutes(durationMinutes);
                
                // 如果订单进行中（status=0且endTime为null），计算当前费用（只返回不更新数据库）
                if (order.getStatus() == 0 && order.getEndTime() == null) {
                    double amount = calculateAmount(order.getStartTime(), now, order.getParkingLotId());
                    order.setAmount(java.math.BigDecimal.valueOf(amount));
                }
            }
        }
        
        return Result.success("获取用户订单列表成功", orders);
    }
    
    /**
     * 驼峰命名转下划线命名
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder underscore = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                underscore.append('_').append(Character.toLowerCase(c));
            } else {
                underscore.append(c);
            }
        }
        return underscore.toString();
    }

    @Log(module = "订单管理", operation = "取消", description = "取消订单")
    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    @Transactional
    public Result<ParkingOrder> cancelOrder(@PathVariable Long id) {
        ParkingOrder order = parkingOrderService.getById(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != 0) { // 只有待进场状态可以取消
            return Result.fail("订单状态不允许取消");
        }
        
        // 检查是否在预约成功后10分钟内
        if (order.getStartTime() != null) {
            long minutesPassed = (System.currentTimeMillis() - order.getStartTime().getTime()) / (60 * 1000);
            if (minutesPassed > 10) {
                return Result.fail("预约成功后超过10分钟，不可取消订单");
            }
        }
        
        // 设置结束时间为当前时间（取消时间）
        Date now = new Date();
        order.setEndTime(now);
        
        // 计算并保存停车时长（分钟）
        if (order.getStartTime() != null) {
            long durationMillis = now.getTime() - order.getStartTime().getTime();
            int durationMinutes = (int) (durationMillis / (60 * 1000));
            order.setDurationMinutes(durationMinutes);
        }
        
        order.setStatus((int) 2); // 2表示已取消
        parkingOrderService.updateById(order);
        return Result.success("订单取消成功", order);
    }

    @Log(module = "订单管理", operation = "完成", description = "完成订单（管理员手动完成）")
    @Operation(summary = "完成订单（管理员手动完成）")
    @PostMapping("/{id}/complete")
    @Transactional
    public Result<ParkingOrder> completeOrder(@PathVariable Long id) {
        ParkingOrder order = parkingOrderService.getById(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != 0) { // 只有待进场状态可以完成
            return Result.fail("订单状态不允许完成");
        }

        // 设置结束时间为当前时间
        Date now = new Date();
        order.setEndTime(now);

        // 计算停车时长（分钟）
        if (order.getStartTime() != null) {
            long durationMillis = now.getTime() - order.getStartTime().getTime();
            int durationMinutes = (int) (durationMillis / (60 * 1000));
            order.setDurationMinutes(durationMinutes);

            // 计算费用：30分钟以内0元，超过30分钟按停车场单价/小时，24h封顶为单价8倍
            double amount = calculateAmount(order.getStartTime(), now, order.getParkingLotId());
            order.setAmount(java.math.BigDecimal.valueOf(amount));
        }

        order.setStatus((int) 1); // 1表示已完成
        parkingOrderService.updateById(order);
        return Result.success("订单完成成功", order);
    }

    @Log(module = "订单管理", operation = "完成并结算", description = "完成订单并结算（用户支付）")
    @Operation(summary = "完成订单并结算（用户支付）")
    @PostMapping("/{id}/completeAndPay")
    public Result<String> completeAndPay(@PathVariable Long id, @RequestParam(required = false) Long couponId) {
        ParkingOrder order = parkingOrderService.getById(id);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (order.getStatus() != 0) { // 只有待进场状态可以完成并结算
            return Result.fail("订单状态不允许完成");
        }

        // 设置结束时间为当前时间
        Date now = new Date();
        order.setEndTime(now);

        // 计算停车时长（分钟）
        if (order.getStartTime() != null) {
            long durationMillis = now.getTime() - order.getStartTime().getTime();
            int durationMinutes = (int) (durationMillis / (60 * 1000));
            order.setDurationMinutes(durationMinutes);

            // 计算费用：30分钟以内0元，超过30分钟按停车场单价/小时，24h封顶为单价8倍
            double amount = calculateAmount(order.getStartTime(), now, order.getParkingLotId());
            order.setAmount(java.math.BigDecimal.valueOf(amount));

            // 如果金额为0，直接完成订单，不需要支付
            if (amount == 0.0) {
                order.setStatus(1); // 直接标记为已完成
                order.setActualAmount(java.math.BigDecimal.ZERO);
                parkingOrderService.updateById(order);
                return Result.success("订单完成成功（免费）", null);
            }
        }

        // 如果有优惠券，先保存到订单中
        if (couponId != null) {
            order.setCouponId(couponId);
        }

        parkingOrderService.updateById(order);

        // 返回支付宝支付页面URL
        String payUrl = alipayProperties.getWebPayUrl() + "?orderId=" + id + "&parkingLotId=" + order.getParkingLotId();
        if (couponId != null) {
            payUrl += "&couponId=" + couponId;
        }

        return Result.success("订单完成成功，请进行支付", payUrl);
    }
    
    /**
     * 计算停车费用
     * 30分钟以内（含30分钟）：0元
     * 超过30分钟：按停车场单价/小时，不足1小时按1小时计算
     * 每24小时封顶：单价8倍
     * 计算方式：从startTime开始，每24小时为一个周期，每个周期封顶为单价8倍
     */
    private double calculateAmount(Date startTime, Date endTime, Long parkingLotId) {
        // 获取停车场单价
        cn.gp.smartparking.model.entity.ParkingLot parkingLot = parkingLotEntityService.getById(parkingLotId);
        if (parkingLot == null || parkingLot.getRate() == null) {
            System.err.println("停车场不存在或单价未设置 - parkingLotId: " + parkingLotId);
            return 0.0;
        }

        double hourlyRate = parkingLot.getRate().doubleValue();
        double dailyCap = hourlyRate * 8; // 24小时封顶为单价8倍

        long durationMillis = endTime.getTime() - startTime.getTime();
        int durationMinutes = (int) (durationMillis / (60 * 1000));

        if (durationMinutes <= 30) {
            return 0.0;
        }

        // 计算跨越的完整24小时周期数
        int full24HourPeriods = durationMinutes / (24 * 60);
        int remainingMinutes = durationMinutes % (24 * 60);

        double totalAmount = 0.0;

        // 每个完整24小时周期按封顶价格计算
        totalAmount += full24HourPeriods * dailyCap;

        // 剩余不足24小时的部分按小时计算（不满1小时按1小时计算）
        if (remainingMinutes > 0) {
            int remainingHours = (int) Math.ceil(remainingMinutes / 60.0);
            double remainingAmount = remainingHours * hourlyRate;
            // 剩余部分也要封顶
            totalAmount += Math.min(remainingAmount, dailyCap);
        }

        return totalAmount;
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

    @Operation(summary = "获取用户停车记录（已完成的订单）")
    @GetMapping("/records")
    public Result<List<ParkingRecordDTO>> getParkingRecords(@RequestParam Long userId) {
        // 查询用户已完成的订单（status=1）
        List<ParkingOrder> orders = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getUserId, userId)
                .eq(ParkingOrder::getStatus, 1) // 只查询已完成的订单
                .orderByDesc(ParkingOrder::getEndTime)
                .list();
        
        List<ParkingRecordDTO> records = new ArrayList<>();
        Date now = new Date();
        
        for (ParkingOrder order : orders) {
            ParkingRecordDTO record = new ParkingRecordDTO();
            record.setId(order.getId());
            record.setPlateNumber(order.getPlateNumber());
            record.setAmount(order.getAmount());
            record.setStartTime(order.getStartTime());
            record.setEndTime(order.getEndTime());
            
            // 动态计算停车时长
            if (order.getStartTime() != null) {
                long endTimeMillis = (order.getEndTime() != null) 
                    ? order.getEndTime().getTime() 
                    : now.getTime();
                long durationMillis = endTimeMillis - order.getStartTime().getTime();
                int durationMinutes = (int) (durationMillis / (60 * 1000));
                record.setDurationMinutes(durationMinutes);
            }
            
            // 获取停车场名称
            if (order.getParkingLotId() != null) {
                ParkingLot parkingLot = parkingLotService.getById(order.getParkingLotId());
                if (parkingLot != null) {
                    record.setParkingLotName(parkingLot.getName());
                }
            }
            
            records.add(record);
        }
        
        return Result.success("获取停车记录成功", records);
    }

}
