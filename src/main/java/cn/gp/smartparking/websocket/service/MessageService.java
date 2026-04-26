package cn.gp.smartparking.websocket.service;

import cn.gp.smartparking.model.entity.Notification;
import cn.gp.smartparking.model.entity.User;
import cn.gp.smartparking.service.NotificationService;
import cn.gp.smartparking.service.UserService;
import cn.gp.smartparking.websocket.entity.WebSocketMessage;
import cn.gp.smartparking.websocket.manager.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * WebSocket 消息服务
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final WebSocketSessionManager sessionManager;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    /**
     * 处理接收到的消息
     */
    public void processMessage(String channelId, WebSocketMessage message) {
        try {
            log.debug("处理消息: channelId={}, type={}", channelId, message.getType());
            
            switch (message.getType()) {
                case WebSocketMessage.MessageType.HEARTBEAT:
                    handleHeartbeat(channelId, message);
                    break;
                default:
                    log.warn("未知的消息类型: {}", message.getType());
                    break;
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(String channelId, WebSocketMessage message) {
        log.debug("收到心跳消息: channelId={}", channelId);
        // 这里可以更新用户的最后心跳时间
    }

    /**
     * 推送停车场更新消息
     */
    public void pushParkingLotUpdate(Long userId, Object parkingLotData) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.PARKING_LOT_UPDATE, 
            parkingLotData, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 推送推荐更新消息
     */
    public void pushRecommendationUpdate(Long userId, Object recommendationData) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.RECOMMENDATION_UPDATE, 
            recommendationData, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 推送预测更新消息
     */
    public void pushPredictionUpdate(Long userId, Object predictionData) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.PREDICTION_UPDATE, 
            predictionData, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 推送订单更新消息
     */
    public void pushOrderUpdate(Long userId, Object orderData) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.ORDER_UPDATE, 
            orderData, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 推送支付更新消息
     */
    public void pushPaymentUpdate(Long userId, Object paymentData) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.PAYMENT_UPDATE, 
            paymentData, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 推送系统通知
     */
    public void pushSystemNotification(Long userId, String notification) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.SYSTEM_NOTIFICATION, 
            notification, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 广播系统通知
     */
    public void broadcastSystemNotification(String notification) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.SYSTEM_NOTIFICATION, 
            notification
        );
        broadcastMessage(message);
    }

    /**
     * 向指定用户发送消息
     */
    private void sendMessageToUser(Long userId, WebSocketMessage message) {
        try {
            log.info("开始发送消息给用户: userId={}, type={}", userId, message.getType());
            
            if (!sessionManager.isUserOnline(userId)) {
                log.warn("用户 {} 不在线，保存到notification表", userId);
                saveNotification(userId, message);
                return;
            }

            String jsonMessage = objectMapper.writeValueAsString(message);
            log.info("WebSocket消息内容: {}", jsonMessage);
            
            boolean success = sessionManager.sendMessageToUser(userId, jsonMessage);
            
            if (success) {
                log.info("消息发送成功: userId={}, type={}", userId, message.getType());
            } else {
                log.error("消息发送失败: userId={}, type={}，保存到notification表", userId, message.getType());
                saveNotification(userId, message);
            }
        } catch (Exception e) {
            log.error("发送消息异常: userId={}", userId, e);
        }
    }

    /**
     * 保存消息到notification表
     */
    private void saveNotification(Long userId, WebSocketMessage message) {
        try {
            log.info("开始保存离线消息: userId={}, type={}", userId, message.getType());
            
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setPushStatus(0); // 未推送
            notification.setIsRead(0); // 未读
            notification.setCreateTime(new Date());

            // 根据消息类型设置通知内容
            if (message.getType().equals(WebSocketMessage.MessageType.COUPON_NOTIFICATION)) {
                notification.setType(2); // 优惠活动
                notification.setTitle("您收到一张新优惠券");
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> data = (java.util.Map<String, Object>) message.getData();
                notification.setBizId(String.valueOf(data.get("couponId")));
                notification.setContent(data.get("name").toString());
                log.info("保存优惠券通知: couponId={}, name={}", data.get("couponId"), data.get("name"));
            } else if (message.getType().equals(WebSocketMessage.MessageType.SYSTEM_NOTIFICATION)) {
                notification.setType(0); // 系统通知
                notification.setTitle("系统通知");
                notification.setContent(message.getData() != null ? message.getData().toString() : "");
                log.info("保存系统通知: content={}", message.getData());
            } else {
                notification.setType(0); // 默认系统通知
                notification.setTitle("新消息");
                notification.setContent(message.getType());
                log.info("保存其他类型通知: type={}", message.getType());
            }

            notificationService.save(notification);
            log.info("消息已保存到notification表: userId={}, type={}, notificationId={}", 
                userId, message.getType(), notification.getId());
        } catch (Exception e) {
            log.error("保存消息到notification表失败: userId={}, type={}", userId, message.getType(), e);
        }
    }

    /**
     * 广播消息
     */
    private void broadcastMessage(WebSocketMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            sessionManager.broadcastMessage(jsonMessage);
            log.info("广播消息发送成功: type={}", message.getType());
        } catch (Exception e) {
            log.error("广播消息异常", e);
        }
    }

    /**
     * 向指定用户类型的用户广播消息
     */
    public void broadcastToUserType(String userType, String message) {
        WebSocketMessage wsMessage = new WebSocketMessage(
            WebSocketMessage.MessageType.SYSTEM_NOTIFICATION, 
            message
        );
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(wsMessage);
            sessionManager.broadcastToUserType(userType, jsonMessage);
            log.info("向用户类型 {} 广播消息成功", userType);
        } catch (Exception e) {
            log.error("向用户类型 {} 广播消息异常", userType, e);
        }
    }

    /**
     * 向车主广播消息（排除管理员）
     */
    public void broadcastToUsers(String message) {
        WebSocketMessage wsMessage = new WebSocketMessage(
            WebSocketMessage.MessageType.SYSTEM_NOTIFICATION, 
            message
        );
        
        try {
            String jsonMessage = objectMapper.writeValueAsString(wsMessage);
            sessionManager.broadcastToUsers(jsonMessage);
            log.info("向车主广播消息成功");
        } catch (Exception e) {
            log.error("向车主广播消息异常", e);
        }
    }

    /**
     * 推送优惠券消息
     */
    public void pushCouponNotification(Long userId, Object couponData) {
        WebSocketMessage message = new WebSocketMessage(
            WebSocketMessage.MessageType.COUPON_NOTIFICATION, 
            couponData, 
            userId
        );
        sendMessageToUser(userId, message);
    }

    /**
     * 向所有车主推送优惠券消息（排除管理员）
     */
    public void broadcastCouponNotification(Object couponData) {
        log.info("开始广播优惠券消息: couponData={}", couponData);

        try {
            // 获取所有非管理员用户（role == 0 表示车主）
            List<User> allUsers = userService.lambdaQuery()
                    .eq(User::getRole, 0)
                    .eq(User::getIsDeleted, 0)
                    .list();
            log.info("非管理员用户数量: {}", allUsers.size());

            // 为每个用户发送消息（在线用户直接推送，离线用户保存到notification表）
            for (User user : allUsers) {
                WebSocketMessage userMessage = new WebSocketMessage(
                    WebSocketMessage.MessageType.COUPON_NOTIFICATION,
                    couponData,
                    user.getId()
                );
                sendMessageToUser(user.getId(), userMessage);
            }

            log.info("向所有车主广播优惠券消息成功");
        } catch (Exception e) {
            log.error("向所有车主广播优惠券消息异常", e);
        }
    }
}
