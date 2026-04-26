package cn.gp.smartparking.zf;

import cn.gp.smartparking.model.entity.Coupon;
import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.service.CouponService;
import cn.gp.smartparking.service.ParkingOrderService;
import com.alipay.api.AlipayApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class PayService {

    private final AlipayWrapper alipayWrapper;
    private final ParkingOrderService parkingOrderService;
    private final CouponService couponService;
    private final cn.gp.smartparking.service.ParkingLotService parkingLotService;

    public PayService(AlipayWrapper alipayWrapper, ParkingOrderService parkingOrderService, CouponService couponService, cn.gp.smartparking.service.ParkingLotService parkingLotService) {
        this.alipayWrapper = alipayWrapper;
        this.parkingOrderService = parkingOrderService;
        this.couponService = couponService;
        this.parkingLotService = parkingLotService;
    }

    public String createWebPayOrder(Long orderId, Long parkingLotId, Long couponId) throws AlipayApiException {
        ParkingOrder order = parkingOrderService.getById(orderId);
        if (order == null) {
            return null;
        }

        // 如果订单金额为null，先计算停车费用
        if (order.getAmount() == null) {
            Date now = new Date();
            
            // 如果startTime为null，使用createTime
            Date startTime = order.getStartTime() != null ? order.getStartTime() : order.getCreateTime();
            
            if (startTime != null) {
                // 设置结束时间为当前时间
                order.setEndTime(now);
                
                // 计算停车时长（分钟）
                long durationMillis = now.getTime() - startTime.getTime();
                int durationMinutes = (int) (durationMillis / (60 * 1000));
                order.setDurationMinutes(durationMinutes);

                // 计算费用：30分钟以内0元，超过30分钟按停车场单价/小时，24h封顶为单价8倍
                double amount = calculateAmount(startTime, now, parkingLotId);
                order.setAmount(java.math.BigDecimal.valueOf(amount));
                
                // 更新订单
                parkingOrderService.updateById(order);
                
                // 重新获取订单以确保金额已更新
                order = parkingOrderService.getById(orderId);
            } else {
                System.err.println("订单startTime和createTime都为null - orderId: " + orderId);
                return null;
            }
        }

        // 确保金额不为null
        if (order.getAmount() == null) {
            System.err.println("订单金额为null - orderId: " + orderId + ", startTime: " + order.getStartTime() + ", createTime: " + order.getCreateTime());
            return null;
        }

        // 如果有优惠券，计算折扣后的金额
        java.math.BigDecimal finalAmount = order.getAmount();
        if (couponId != null) {
            Coupon coupon = couponService.getById(couponId);
            if (coupon != null && coupon.getStatus() == 0 && coupon.getEndTime().after(new Date())) {
                // 验证优惠券是否属于当前用户且满足最低消费
                if (coupon.getUserId().equals(order.getUserId()) &&
                    (coupon.getMinAmount() == null || coupon.getMinAmount().compareTo(java.math.BigDecimal.ZERO) == 0 ||
                     order.getAmount().compareTo(coupon.getMinAmount()) >= 0)) {
                    // 满减券直接减去优惠值
                    if (coupon.getType() == 0) {
                        finalAmount = order.getAmount().subtract(coupon.getValue());
                        if (finalAmount.compareTo(java.math.BigDecimal.ZERO) < 0) {
                            finalAmount = java.math.BigDecimal.ZERO;
                        }
                    }
                    // 将优惠券ID保存到订单中，支付成功后再使用
                    order.setCouponId(couponId);
                }
            }
        }

        // 如果金额为0，直接完成订单，不需要支付
        if (finalAmount.compareTo(java.math.BigDecimal.ZERO) == 0) {
            order.setStatus(1); // 直接标记为已完成
            order.setActualAmount(java.math.BigDecimal.ZERO);
            parkingOrderService.updateById(order);
            return null; // 返回null表示免费订单
        }

        String outTradeNo = generateTradeNo();
        String subject = "停车场缴费-" + order.getPlateNumber();
        String body = "停车场:" + order.getParkingLotId() + ",车位:" + order.getSpaceNo();

        String payForm = alipayWrapper.buildWebPayRequest(
                outTradeNo,
                finalAmount,
                subject,
                body
        );

        order.setOrderNo(outTradeNo);
        order.setActualAmount(finalAmount); // 设置实付金额为折扣后金额，不修改原金额
        parkingOrderService.updateById(order);

        return payForm;
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
        cn.gp.smartparking.model.entity.ParkingLot parkingLot = parkingLotService.getById(parkingLotId);
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

        // 剩余不足24小时的部分按小时计算
        if (remainingMinutes > 30) {
            int remainingHours = (int) Math.ceil((remainingMinutes - 30) / 60.0);
            double remainingAmount = remainingHours * hourlyRate;
            // 剩余部分也要封顶
            totalAmount += Math.min(remainingAmount, dailyCap);
        }

        return totalAmount;
    }

    public String createMobilePayOrder(Long orderId, Long parkingLotId) throws AlipayApiException {
        ParkingOrder order = parkingOrderService.getById(orderId);
        if (order == null) {
            return null;
        }

        // 如果订单金额为null，先计算停车费用
        if (order.getAmount() == null) {
            Date now = new Date();
            
            // 如果startTime为null，使用createTime
            Date startTime = order.getStartTime() != null ? order.getStartTime() : order.getCreateTime();
            
            if (startTime != null) {
                // 设置结束时间为当前时间
                order.setEndTime(now);
                
                // 计算停车时长（分钟）
                long durationMillis = now.getTime() - startTime.getTime();
                int durationMinutes = (int) (durationMillis / (60 * 1000));
                order.setDurationMinutes(durationMinutes);

                // 计算费用：30分钟以内0元，超过30分钟按停车场单价/小时，24h封顶为单价8倍
                double amount = calculateAmount(startTime, now, parkingLotId);
                order.setAmount(java.math.BigDecimal.valueOf(amount));
                
                // 更新订单
                parkingOrderService.updateById(order);
                
                // 重新获取订单以确保金额已更新
                order = parkingOrderService.getById(orderId);
            } else {
                System.err.println("订单startTime和createTime都为null - orderId: " + orderId);
                return null;
            }
        }

        // 确保金额不为null
        if (order.getAmount() == null) {
            System.err.println("订单金额为null - orderId: " + orderId + ", startTime: " + order.getStartTime() + ", createTime: " + order.getCreateTime());
            return null;
        }

        // 如果金额为0，直接完成订单，不需要支付
        if (order.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            order.setStatus(1); // 直接标记为已完成
            order.setActualAmount(java.math.BigDecimal.ZERO);
            parkingOrderService.updateById(order);
            return null; // 返回null表示免费订单
        }

        String outTradeNo = generateTradeNo();
        String subject = "停车场缴费-" + order.getPlateNumber();
        String body = "停车场:" + order.getParkingLotId() + ",车位:" + order.getSpaceNo();

        String orderString = alipayWrapper.buildAppPayRequest(
                outTradeNo,
                order.getAmount(),
                subject,
                body
        );

        order.setOrderNo(outTradeNo);
        order.setActualAmount(order.getAmount());
        parkingOrderService.updateById(order);

        return orderString;
    }

    public String queryPayResult(String outTradeNo) throws AlipayApiException {
        return alipayWrapper.queryTradeStatus(outTradeNo);
    }

    public boolean processNotify(java.util.Map<String, String> params) {
        try {
            String tradeStatus = params.get("trade_status");
            String outTradeNo = params.get("out_trade_no");

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                updateOrderStatus(outTradeNo, 1);
                System.out.println("支付回调处理成功 - outTradeNo: " + outTradeNo);
                return true;
            }
        } catch (Exception e) {
            System.err.println("支付回调处理异常: " + e.getMessage());
        }
        return false;
    }

    @Transactional
    private void updateOrderStatus(String orderNo, int status) {
        ParkingOrder order = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getOrderNo, orderNo)
                .one();
        if (order != null) {
            order.setStatus(status);
            if (status == 1) {
                order.setEndTime(new Date());

                // 支付成功时，如果订单有关联的优惠券，则使用优惠券
                if (order.getCouponId() != null) {
                    Coupon coupon = couponService.getById(order.getCouponId());
                    if (coupon != null && coupon.getStatus() == 0) {
                        coupon.setStatus(1); // 标记为已使用
                        coupon.setUsedTime(new Date());
                        coupon.setUsedOrderId(order.getId());
                        couponService.updateById(coupon);
                        System.out.println("优惠券使用成功 - couponId: " + coupon.getId() + ", orderId: " + order.getId());
                    }
                }
            }
            parkingOrderService.updateById(order);
        }
    }

    private String generateTradeNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "PK" + time + random;
    }

    public static class MobileReturnResponse {
        private String tradeNo;
        private String status;
        private String tradeStatus;

        public MobileReturnResponse() {}

        public MobileReturnResponse(String tradeNo, String status, String tradeStatus) {
            this.tradeNo = tradeNo;
            this.status = status;
            this.tradeStatus = tradeStatus;
        }

        public String getTradeNo() { return tradeNo; }
        public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getTradeStatus() { return tradeStatus; }
        public void setTradeStatus(String tradeStatus) { this.tradeStatus = tradeStatus; }
    }
}
