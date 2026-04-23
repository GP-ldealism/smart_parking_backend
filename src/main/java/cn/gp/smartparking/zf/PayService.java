package cn.gp.smartparking.zf;

import cn.gp.smartparking.model.entity.ParkingOrder;
import cn.gp.smartparking.service.ParkingOrderService;
import com.alipay.api.AlipayApiException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class PayService {

    private final AlipayWrapper alipayWrapper;
    private final ParkingOrderService parkingOrderService;

    public PayService(AlipayWrapper alipayWrapper, ParkingOrderService parkingOrderService) {
        this.alipayWrapper = alipayWrapper;
        this.parkingOrderService = parkingOrderService;
    }

    public String createWebPayOrder(Long orderId) throws AlipayApiException {
        ParkingOrder order = parkingOrderService.getById(orderId);
        if (order == null) {
            return null;
        }

        String outTradeNo = generateTradeNo();
        String subject = "停车场缴费-" + order.getPlateNumber();
        String body = "停车场:" + order.getParkingLotId() + ",车位:" + order.getSpaceNo();

        String payForm = alipayWrapper.buildWebPayRequest(
                outTradeNo,
                order.getAmount(),
                subject,
                body
        );

        order.setOrderNo(outTradeNo);
        parkingOrderService.updateById(order);

        return payForm;
    }

    public String createMobilePayOrder(Long orderId) throws AlipayApiException {
        ParkingOrder order = parkingOrderService.getById(orderId);
        if (order == null) {
            return null;
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

    private void updateOrderStatus(String orderNo, int status) {
        ParkingOrder order = parkingOrderService.lambdaQuery()
                .eq(ParkingOrder::getOrderNo, orderNo)
                .one();
        if (order != null) {
            order.setStatus(status);
            if (status == 1) {
                order.setEndTime(new Date());
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
