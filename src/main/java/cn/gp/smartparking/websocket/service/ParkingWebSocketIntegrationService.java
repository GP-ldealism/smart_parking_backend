package cn.gp.smartparking.websocket.service;

import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.algorithm.entity.RecommendResult;
import cn.gp.smartparking.algorithm.entity.PredictionResult;
import cn.gp.smartparking.websocket.entity.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 停车WebSocket集成服务
 * 将WebSocket集成到现有业务系统中
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingWebSocketIntegrationService {

    private final MessageService messageService;

    /**
     * 推送停车场状态更新
     */
    public void pushParkingLotStatusUpdate(Long userId, ParkingLot parkingLot) {
        try {
            log.info("推送停车场状态更新: userId={}, parkingLotId={}", userId, parkingLot.getId());
            messageService.pushParkingLotUpdate(userId, parkingLot);
        } catch (Exception e) {
            log.error("推送停车场状态更新失败", e);
        }
    }

    /**
     * 批量推送停车场状态更新
     */
    public void pushParkingLotStatusBatchUpdate(List<Long> userIds, List<ParkingLot> parkingLots) {
        try {
            log.info("批量推送停车场状态更新: userIds.size={}, parkingLots.size={}", 
                userIds.size(), parkingLots.size());
            
            for (Long userId : userIds) {
                messageService.pushParkingLotUpdate(userId, parkingLots);
            }
        } catch (Exception e) {
            log.error("批量推送停车场状态更新失败", e);
        }
    }

    /**
     * 推送推荐结果更新
     */
    public void pushRecommendationUpdate(Long userId, List<RecommendResult> recommendations) {
        try {
            log.info("推送推荐结果更新: userId={}, recommendations.size={}", 
                userId, recommendations.size());
            messageService.pushRecommendationUpdate(userId, recommendations);
        } catch (Exception e) {
            log.error("推送推荐结果更新失败", e);
        }
    }

    /**
     * 推送预测结果更新
     */
    public void pushPredictionUpdate(Long userId, List<PredictionResult> predictions) {
        try {
            log.info("推送预测结果更新: userId={}, predictions.size={}", 
                userId, predictions.size());
            messageService.pushPredictionUpdate(userId, predictions);
        } catch (Exception e) {
            log.error("推送预测结果更新失败", e);
        }
    }

    /**
     * 推送订单状态更新
     */
    public void pushOrderStatusUpdate(Long userId, Object orderData) {
        try {
            log.info("推送订单状态更新: userId={}", userId);
            messageService.pushOrderUpdate(userId, orderData);
        } catch (Exception e) {
            log.error("推送订单状态更新失败", e);
        }
    }

    /**
     * 推送支付状态更新
     */
    public void pushPaymentStatusUpdate(Long userId, Object paymentData) {
        try {
            log.info("推送支付状态更新: userId={}", userId);
            messageService.pushPaymentUpdate(userId, paymentData);
        } catch (Exception e) {
            log.error("推送支付状态更新失败", e);
        }
    }

    /**
     * 广播系统通知
     */
    public void broadcastSystemNotification(String message) {
        try {
            log.info("广播系统通知: {}", message);
            messageService.broadcastSystemNotification(message);
        } catch (Exception e) {
            log.error("广播系统通知失败", e);
        }
    }

    /**
     * 向管理员发送通知
     */
    public void notifyAdmins(String message) {
        try {
            log.info("向管理员发送通知: {}", message);
            messageService.broadcastToUserType("admin", message);
        } catch (Exception e) {
            log.error("向管理员发送通知失败", e);
        }
    }

    /**
     * 向用户发送个性化通知
     */
    public void notifyUser(Long userId, String message) {
        try {
            log.info("向用户发送通知: userId={}, message={}", userId, message);
            messageService.pushSystemNotification(userId, message);
        } catch (Exception e) {
            log.error("向用户发送通知失败", e);
        }
    }

    /**
     * 向车主广播系统公告（排除管理员）
     */
    public void broadcastToUsers(String message) {
        try {
            log.info("向车主广播系统公告: {}", message);
            messageService.broadcastToUsers(message);
        } catch (Exception e) {
            log.error("向车主广播系统公告失败", e);
        }
    }

    /**
     * 推送优惠券通知
     */
    public void pushCouponNotification(Long userId, Object couponData) {
        try {
            log.info("推送优惠券通知: userId={}", userId);
            messageService.pushCouponNotification(userId, couponData);
        } catch (Exception e) {
            log.error("推送优惠券通知失败", e);
        }
    }

    /**
     * 向所有车主广播优惠券通知
     */
    public void broadcastCouponNotification(Object couponData) {
        try {
            log.info("向所有车主广播优惠券通知");
            messageService.broadcastCouponNotification(couponData);
        } catch (Exception e) {
            log.error("向所有车主广播优惠券通知失败", e);
        }
    }
}
