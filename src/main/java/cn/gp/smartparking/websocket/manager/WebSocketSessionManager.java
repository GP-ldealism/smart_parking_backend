package cn.gp.smartparking.websocket.manager;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * WebSocket 会话管理器
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 用户ID到Channel的映射
     */
    private final ConcurrentMap<Long, Channel> userChannels = new ConcurrentHashMap<>();
    
    /**
     * Channel到用户ID的映射
     */
    private final ConcurrentMap<String, Long> channelUsers = new ConcurrentHashMap<>();
    
    /**
     * 存储用户会话信息
     */
    private final ConcurrentMap<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     */
    public void addUserSession(Long userId, Channel channel, String userType) {
        userChannels.put(userId, channel);
        channelUsers.put(channel.id().asShortText(), userId);
        
        UserSession session = new UserSession(userId, channel.id().asShortText(), userType, System.currentTimeMillis());
        userSessions.put(userId, session);
        
        log.info("用户 {} WebSocket 会话已建立，Channel: {}", userId, channel.id().asShortText());
    }

    /**
     * 移除用户会话
     */
    public void removeSession(Channel channel) {
        Long userId = channelUsers.remove(channel.id().asShortText());
        if (userId != null) {
            userChannels.remove(userId);
            userSessions.remove(userId);
            log.info("用户 {} WebSocket 会话已移除", userId);
        }
    }

    /**
     * 根据用户ID获取Channel
     */
    public Channel getUserChannel(Long userId) {
        return userChannels.get(userId);
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        Channel channel = userChannels.get(userId);
        return channel != null && channel.isActive();
    }

    /**
     * 获取在线用户列表
     */
    public List<Long> getOnlineUsers() {
        return userChannels.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return (int) userChannels.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .count();
    }

    /**
     * 向指定用户发送消息
     */
    public boolean sendMessageToUser(Long userId, String message) {
        Channel channel = userChannels.get(userId);
        if (channel != null && channel.isActive()) {
            try {
                channel.writeAndFlush(new TextWebSocketFrame(message));
                return true;
            } catch (Exception e) {
                log.error("向用户 {} 发送消息失败", userId, e);
                return false;
            }
        }
        return false;
    }

    /**
     * 向所有在线用户广播消息
     */
    public void broadcastMessage(String message) {
        userChannels.values().stream()
                .filter(Channel::isActive)
                .forEach(channel -> {
                    try {
                        channel.writeAndFlush(new TextWebSocketFrame(message));
                    } catch (Exception e) {
                        log.error("广播消息失败，Channel: {}", channel.id(), e);
                    }
                });
    }

    /**
     * 向指定类型的用户广播消息
     */
    public void broadcastToUserType(String userType, String message) {
        userSessions.entrySet().stream()
                .filter(entry -> userType.equals(entry.getValue().getUserType()))
                .map(Map.Entry::getKey)
                .forEach(userId -> sendMessageToUser(userId, message));
    }

    /**
     * 向车主广播消息（排除管理员）
     */
    public void broadcastToUsers(String message) {
        userSessions.entrySet().stream()
                .filter(entry -> "user".equals(entry.getValue().getUserType()))
                .map(Map.Entry::getKey)
                .forEach(userId -> sendMessageToUser(userId, message));
    }

    /**
     * 获取用户会话信息
     */
    public UserSession getUserSession(Long userId) {
        return userSessions.get(userId);
    }

    /**
     * 用户会话信息
     */
    public static class UserSession {
        private final Long userId;
        private final String channelId;
        private final String userType;
        private final long connectTime;
        private long lastHeartbeatTime;

        public UserSession(Long userId, String channelId, String userType, long connectTime) {
            this.userId = userId;
            this.channelId = channelId;
            this.userType = userType;
            this.connectTime = connectTime;
            this.lastHeartbeatTime = connectTime;
        }

        // Getters
        public Long getUserId() { return userId; }
        public String getChannelId() { return channelId; }
        public String getUserType() { return userType; }
        public long getConnectTime() { return connectTime; }
        public long getLastHeartbeatTime() { return lastHeartbeatTime; }
        
        public void updateHeartbeatTime() {
            this.lastHeartbeatTime = System.currentTimeMillis();
        }
    }
}
