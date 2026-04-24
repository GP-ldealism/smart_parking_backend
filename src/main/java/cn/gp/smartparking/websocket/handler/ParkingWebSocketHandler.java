package cn.gp.smartparking.websocket.handler;

import cn.gp.smartparking.common.JwtUtils;
import cn.gp.smartparking.model.entity.Notification;
import cn.gp.smartparking.service.NotificationService;
import cn.gp.smartparking.websocket.entity.WebSocketMessage;
import cn.gp.smartparking.websocket.manager.WebSocketSessionManager;
import cn.gp.smartparking.websocket.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketCloseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 停车 WebSocket 消息处理器
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
public class ParkingWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> implements ApplicationContextAware {

    private WebSocketSessionManager sessionManager;
    private MessageService messageService;
    private ObjectMapper objectMapper;
    private ApplicationContext applicationContext;
    private NotificationService notificationService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        // 在这里初始化依赖
        this.sessionManager = applicationContext.getBean(WebSocketSessionManager.class);
        this.messageService = applicationContext.getBean(MessageService.class);
        this.objectMapper = applicationContext.getBean(ObjectMapper.class);
        this.notificationService = applicationContext.getBean(NotificationService.class);
    }
    
    /**
     * 手动设置依赖（用于非Spring管理的实例）
     */
    public void setDependencies(WebSocketSessionManager sessionManager, 
                                MessageService messageService, 
                                ObjectMapper objectMapper,
                                NotificationService notificationService) {
        this.sessionManager = sessionManager;
        this.messageService = messageService;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("WebSocket 连接建立: channelId={}, remoteAddress={}", 
            ctx.channel().id(), ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("WebSocket 事件触发: evt={}", evt.getClass().getSimpleName());
        
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete handshake = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            log.info("WebSocket 握手完成: channelId={}, uri={}", 
                ctx.channel().id(), handshake.requestUri());
            
            // 从URL参数中获取token
            String uri = handshake.requestUri();
            String token = null;
            if (uri != null && uri.contains("token=")) {
                token = uri.substring(uri.indexOf("token=") + 6);
                // 去除可能的后续参数
                int ampIndex = token.indexOf('&');
                if (ampIndex != -1) {
                    token = token.substring(0, ampIndex);
                }
                log.debug("从URL中提取到token: {}", token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null");
            }
            
            if (token != null && !token.isEmpty()) {
                try {
                    Long userId = JwtUtils.getUserIdFromToken(token);
                    if (userId != null) {
                        log.info("WebSocket连接用户认证成功: channelId={}, userId={}", ctx.channel().id(), userId);
                        // 存储用户ID到channel属性
                        ctx.channel().attr(io.netty.util.AttributeKey.valueOf("userId")).set(userId);
                        // 推送离线消息
                        log.info("开始推送离线消息: userId={}", userId);
                        pushUnreadNotifications(userId, ctx);
                    } else {
                        log.warn("Token解析失败，userId为null: channelId={}", ctx.channel().id());
                    }
                } catch (Exception e) {
                    log.error("解析token失败: channelId={}", ctx.channel().id(), e);
                }
            } else {
                log.warn("WebSocket连接未提供token: channelId={}", ctx.channel().id());
            }
        } else if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                log.info("客户端读空闲，关闭连接: {}", ctx.channel().id());
                ctx.close();
            } else if (event.state() == IdleState.WRITER_IDLE) {
                // 发送心跳
                WebSocketMessage heartbeat = new WebSocketMessage(WebSocketMessage.MessageType.HEARTBEAT, null);
                sendMessage(ctx, heartbeat);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("WebSocket 连接断开: {}", ctx.channel().id());
        sessionManager.removeSession(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        try {
            String text = msg.text();
            log.debug("收到 WebSocket 消息: {}", text);
            
            WebSocketMessage webSocketMessage = objectMapper.readValue(text, WebSocketMessage.class);
            
            // 处理不同类型的消息
            handleMessage(ctx, webSocketMessage);
            
        } catch (Exception e) {
            log.error("处理 WebSocket 消息失败", e);
            sendErrorResponse(ctx, "消息格式错误");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WebSocket 异常", cause);
        ctx.close();
    }

    /**
     * 处理消息
     */
    private void handleMessage(ChannelHandlerContext ctx, WebSocketMessage message) {
        switch (message.getType()) {
            case WebSocketMessage.MessageType.HEARTBEAT:
                handleHeartbeat(ctx, message);
                break;
            default:
                handleBusinessMessage(ctx, message);
                break;
        }
    }

    /**
     * 处理心跳消息
     */
    private void handleHeartbeat(ChannelHandlerContext ctx, WebSocketMessage message) {
        WebSocketMessage response = new WebSocketMessage(WebSocketMessage.MessageType.HEARTBEAT, "pong");
        sendMessage(ctx, response);
    }

    /**
     * 处理业务消息
     */
    private void handleBusinessMessage(ChannelHandlerContext ctx, WebSocketMessage message) {
        // 这里可以添加具体的业务逻辑处理
        log.info("处理业务消息: type={}, data={}", message.getType(), message.getData());
        
        // 可以根据消息类型调用不同的服务
        messageService.processMessage(ctx.channel().id().asShortText(), message);
    }
    
    /**
     * 推送未读通知
     */
    private void pushUnreadNotifications(Long userId, ChannelHandlerContext ctx) {
        try {
            log.info("开始查询未推送通知: userId={}", userId);
            
            // 使用MyBatis-Plus的查询方式
            List<Notification> unreadNotifications = notificationService.list(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification>()
                    .eq(Notification::getUserId, userId)
                    .eq(Notification::getPushStatus, 0)
                    .eq(Notification::getIsDeleted, 0)
                    .orderByAsc(Notification::getCreateTime)
            );
            
            if (unreadNotifications.isEmpty()) {
                log.info("用户 {} 没有未推送的通知", userId);
                return;
            }
            
            log.info("用户 {} 有 {} 条未推送的通知", userId, unreadNotifications.size());
            
            for (Notification notification : unreadNotifications) {
                try {
                    log.info("准备推送通知: notificationId={}, type={}, title={}", 
                        notification.getId(), notification.getType(), notification.getTitle());
                    
                    WebSocketMessage wsMessage = buildWebSocketMessage(notification);
                    sendMessage(ctx, wsMessage);
                    
                    // 标记为已推送
                    notification.setPushStatus(1);
                    notification.setPushTime(new java.util.Date());
                    notificationService.updateById(notification);
                    
                    log.info("通知推送成功: notificationId={}, userId={}", notification.getId(), userId);
                } catch (Exception e) {
                    log.error("通知推送失败: notificationId={}, userId={}", notification.getId(), userId, e);
                    
                    // 标记为推送失败
                    notification.setPushStatus(2);
                    notification.setPushFailReason(e.getMessage());
                    notificationService.updateById(notification);
                }
            }
        } catch (Exception e) {
            log.error("推送未读通知异常: userId={}", userId, e);
        }
    }
    
    /**
     * 根据通知构建WebSocket消息
     */
    private WebSocketMessage buildWebSocketMessage(Notification notification) {
        WebSocketMessage wsMessage = new WebSocketMessage();
        wsMessage.setTargetUserId(notification.getUserId());
        
        // 根据通知类型设置消息类型
        if (notification.getType() == 2) {
            // 优惠活动
            wsMessage.setType(WebSocketMessage.MessageType.COUPON_NOTIFICATION);
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("notificationId", notification.getId());
            data.put("title", notification.getTitle());
            data.put("content", notification.getContent());
            data.put("bizId", notification.getBizId());
            wsMessage.setData(data);
        } else if (notification.getType() == 0) {
            // 系统通知
            wsMessage.setType(WebSocketMessage.MessageType.SYSTEM_NOTIFICATION);
            wsMessage.setData(notification.getContent());
        } else {
            // 其他类型
            wsMessage.setType(WebSocketMessage.MessageType.SYSTEM_NOTIFICATION);
            wsMessage.setData(notification.getContent());
        }
        
        return wsMessage;
    }

    /**
     * 发送消息
     */
    public void sendMessage(ChannelHandlerContext ctx, WebSocketMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            TextWebSocketFrame frame = new TextWebSocketFrame(json);
            ctx.writeAndFlush(frame);
            log.info("发送 WebSocket 消息成功: {}", json);
        } catch (Exception e) {
            log.error("发送 WebSocket 消息失败", e);
        }
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, String errorMessage) {
        WebSocketMessage error = new WebSocketMessage(WebSocketMessage.MessageType.ERROR, errorMessage);
        sendMessage(ctx, error);
    }
}
