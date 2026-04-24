package cn.gp.smartparking.websocket.config;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import cn.gp.smartparking.websocket.handler.ParkingWebSocketHandler;
import cn.gp.smartparking.websocket.manager.WebSocketSessionManager;
import cn.gp.smartparking.websocket.service.MessageService;
import cn.gp.smartparking.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket 通道初始化器
 * @since 2026-04-24
 */
@RequiredArgsConstructor
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final String websocketPath;
    private final ApplicationContext applicationContext;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        
        // 添加日志处理器
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        
        // 添加心跳检测处理器
        pipeline.addLast(new IdleStateHandler(60, 30, 90, TimeUnit.SECONDS));
        
        // HTTP 编解码器
        pipeline.addLast(new HttpServerCodec());
        
        // HTTP 对象聚合器
        pipeline.addLast(new HttpObjectAggregator(65536));
        
        // WebSocket 协议处理器
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true, 65536));
        
        // 自定义 WebSocket 处理器 - 每个连接创建新实例并注入依赖
        WebSocketSessionManager sessionManager = applicationContext.getBean(WebSocketSessionManager.class);
        MessageService messageService = applicationContext.getBean(MessageService.class);
        ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
        NotificationService notificationService = applicationContext.getBean(NotificationService.class);
        
        ParkingWebSocketHandler handler = new ParkingWebSocketHandler();
        handler.setApplicationContext(applicationContext);
        handler.setDependencies(sessionManager, messageService, objectMapper, notificationService);
        pipeline.addLast(handler);
    }
}
