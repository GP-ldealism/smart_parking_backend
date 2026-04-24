package cn.gp.smartparking.websocket.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Netty WebSocket 配置类
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(WebSocketProperties.class)
@RequiredArgsConstructor
public class NettyWebSocketConfig {

    private final WebSocketProperties webSocketProperties;
    private final ApplicationContext applicationContext;

    /**
     * Netty WebSocket 服务器配置
     */
    @Bean
    public ServerBootstrap nettyWebSocketServer() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        
        // 配置主从线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        
        bootstrap
            .group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(new WebSocketChannelInitializer(webSocketProperties.getPath(), applicationContext));
        
        log.info("Netty WebSocket 服务器配置完成，端口: {}, 路径: {}", 
            webSocketProperties.getPort(), webSocketProperties.getPath());
        return bootstrap;
    }

    /**
     * 启动 Netty WebSocket 服务器
     */
    @Bean
    public NettyWebSocketServer nettyWebSocketServerRunner(ServerBootstrap serverBootstrap) {
        return new NettyWebSocketServer(serverBootstrap, webSocketProperties.getPort(), webSocketProperties.getPath(), applicationContext);
    }
}
