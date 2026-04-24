package cn.gp.smartparking.websocket.listener;

import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.algorithm.entity.RecommendResult;
import cn.gp.smartparking.algorithm.entity.PredictionResult;
import cn.gp.smartparking.websocket.service.ParkingWebSocketIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 停车业务事件监听器
 * 监听业务事件并自动推送WebSocket消息
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ParkingBusinessEventListener {

    private final ParkingWebSocketIntegrationService webSocketIntegrationService;

    /**
     * 监听停车场状态更新事件
     */
    @EventListener
    @Async
    public void handleParkingLotStatusUpdate(ParkingLotStatusUpdateEvent event) {
        try {
            log.debug("处理停车场状态更新事件: {}", event);
            
            // 推送给相关用户
            webSocketIntegrationService.pushParkingLotStatusUpdate(
                event.getUserId(), 
                event.getParkingLot()
            );
            
            // 如果是重要变化，广播通知
            if (event.isImportantChange()) {
                String message = String.format("停车场 %s 状态已更新", event.getParkingLot().getName());
                webSocketIntegrationService.broadcastSystemNotification(message);
            }
            
        } catch (Exception e) {
            log.error("处理停车场状态更新事件失败", e);
        }
    }

    /**
     * 监听推荐结果更新事件
     */
    @EventListener
    @Async
    public void handleRecommendationUpdate(RecommendationUpdateEvent event) {
        try {
            log.debug("处理推荐结果更新事件: {}", event);
            
            webSocketIntegrationService.pushRecommendationUpdate(
                event.getUserId(), 
                event.getRecommendations()
            );
            
        } catch (Exception e) {
            log.error("处理推荐结果更新事件失败", e);
        }
    }

    /**
     * 监听预测结果更新事件
     */
    @EventListener
    @Async
    public void handlePredictionUpdate(PredictionUpdateEvent event) {
        try {
            log.debug("处理预测结果更新事件: {}", event);
            
            webSocketIntegrationService.pushPredictionUpdate(
                event.getUserId(), 
                event.getPredictions()
            );
            
        } catch (Exception e) {
            log.error("处理预测结果更新事件失败", e);
        }
    }

    /**
     * 监听订单状态更新事件
     */
    @EventListener
    @Async
    public void handleOrderStatusUpdate(OrderStatusUpdateEvent event) {
        try {
            log.debug("处理订单状态更新事件: {}", event);
            
            webSocketIntegrationService.pushOrderStatusUpdate(
                event.getUserId(), 
                event.getOrderData()
            );
            
            // 订单完成时发送通知
            if (event.isCompleted()) {
                String message = "您的停车订单已完成";
                webSocketIntegrationService.notifyUser(event.getUserId(), message);
            }
            
        } catch (Exception e) {
            log.error("处理订单状态更新事件失败", e);
        }
    }

    /**
     * 监听支付状态更新事件
     */
    @EventListener
    @Async
    public void handlePaymentStatusUpdate(PaymentStatusUpdateEvent event) {
        try {
            log.debug("处理支付状态更新事件: {}", event);
            
            webSocketIntegrationService.pushPaymentStatusUpdate(
                event.getUserId(), 
                event.getPaymentData()
            );
            
            // 支付成功时发送通知
            if (event.isSuccess()) {
                String message = "支付成功";
                webSocketIntegrationService.notifyUser(event.getUserId(), message);
            }
            
        } catch (Exception e) {
            log.error("处理支付状态更新事件失败", e);
        }
    }

    // 事件定义类
    public static class ParkingLotStatusUpdateEvent {
        private final Long userId;
        private final ParkingLot parkingLot;
        private final boolean importantChange;

        public ParkingLotStatusUpdateEvent(Long userId, ParkingLot parkingLot, boolean importantChange) {
            this.userId = userId;
            this.parkingLot = parkingLot;
            this.importantChange = importantChange;
        }

        // Getters
        public Long getUserId() { return userId; }
        public ParkingLot getParkingLot() { return parkingLot; }
        public boolean isImportantChange() { return importantChange; }

        @Override
        public String toString() {
            return "ParkingLotStatusUpdateEvent{" +
                    "userId=" + userId +
                    ", parkingLotId=" + parkingLot.getId() +
                    ", importantChange=" + importantChange +
                    '}';
        }
    }

    public static class RecommendationUpdateEvent {
        private final Long userId;
        private final List<RecommendResult> recommendations;

        public RecommendationUpdateEvent(Long userId, List<RecommendResult> recommendations) {
            this.userId = userId;
            this.recommendations = recommendations;
        }

        // Getters
        public Long getUserId() { return userId; }
        public List<RecommendResult> getRecommendations() { return recommendations; }

        @Override
        public String toString() {
            return "RecommendationUpdateEvent{" +
                    "userId=" + userId +
                    ", recommendations=" + recommendations +
                    '}';
        }
    }

    public static class PredictionUpdateEvent {
        private final Long userId;
        private final List<PredictionResult> predictions;

        public PredictionUpdateEvent(Long userId, List<PredictionResult> predictions) {
            this.userId = userId;
            this.predictions = predictions;
        }

        // Getters
        public Long getUserId() { return userId; }
        public List<PredictionResult> getPredictions() { return predictions; }

        @Override
        public String toString() {
            return "PredictionUpdateEvent{" +
                    "userId=" + userId +
                    ", predictions=" + predictions +
                    '}';
        }
    }

    public static class OrderStatusUpdateEvent {
        private final Long userId;
        private final Object orderData;
        private final boolean completed;

        public OrderStatusUpdateEvent(Long userId, Object orderData, boolean completed) {
            this.userId = userId;
            this.orderData = orderData;
            this.completed = completed;
        }

        // Getters
        public Long getUserId() { return userId; }
        public Object getOrderData() { return orderData; }
        public boolean isCompleted() { return completed; }

        @Override
        public String toString() {
            return "OrderStatusUpdateEvent{" +
                    "userId=" + userId +
                    ", completed=" + completed +
                    '}';
        }
    }

    public static class PaymentStatusUpdateEvent {
        private final Long userId;
        private final Object paymentData;
        private final boolean success;

        public PaymentStatusUpdateEvent(Long userId, Object paymentData, boolean success) {
            this.userId = userId;
            this.paymentData = paymentData;
            this.success = success;
        }

        // Getters
        public Long getUserId() { return userId; }
        public Object getPaymentData() { return paymentData; }
        public boolean isSuccess() { return success; }

        @Override
        public String toString() {
            return "PaymentStatusUpdateEvent{" +
                    "userId=" + userId +
                    ", success=" + success +
                    '}';
        }
    }
}
