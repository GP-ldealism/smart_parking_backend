package cn.gp.smartparking.websocket.entity;

import lombok.Data;

/**
 * WebSocket 消息实体
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Data
public class WebSocketMessage {
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 消息内容
     */
    private Object data;
    
    /**
     * 目标用户ID（可选）
     */
    private Long targetUserId;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 消息ID
     */
    private String messageId;
    
    public WebSocketMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public WebSocketMessage(String type, Object data) {
        this();
        this.type = type;
        this.data = data;
    }
    
    public WebSocketMessage(String type, Object data, Long targetUserId) {
        this(type, data);
        this.targetUserId = targetUserId;
    }
    
    /**
     * 消息类型常量
     */
    public static class MessageType {
        public static final String PARKING_LOT_UPDATE = "parking_lot_update";
        public static final String RECOMMENDATION_UPDATE = "recommendation_update";
        public static final String PREDICTION_UPDATE = "prediction_update";
        public static final String ORDER_UPDATE = "order_update";
        public static final String PAYMENT_UPDATE = "payment_update";
        public static final String SYSTEM_NOTIFICATION = "system_notification";
        public static final String COUPON_NOTIFICATION = "coupon_notification";
        public static final String HEARTBEAT = "heartbeat";
        public static final String ERROR = "error";
    }
}
