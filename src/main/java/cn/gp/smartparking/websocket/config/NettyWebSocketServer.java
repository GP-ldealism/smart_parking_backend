package cn.gp.smartparking.websocket.config;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Netty WebSocket 服务器启动类
 * 
 * @author SmartParking
 * @since 2026-04-24
 */
@Slf4j
public class NettyWebSocketServer {

    private final ServerBootstrap serverBootstrap;
    private final int port;
    private final String websocketPath;
    private final ApplicationContext applicationContext;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;

    public NettyWebSocketServer(ServerBootstrap serverBootstrap, int port, String websocketPath, ApplicationContext applicationContext) {
        this.serverBootstrap = serverBootstrap;
        this.port = port;
        this.websocketPath = websocketPath;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void start() {
        try {
            log.info("开始启动 Netty WebSocket 服务器，端口: {}, 路径: {}", port, websocketPath);
            
            // 创建新的线程组（不重复设置Bootstrap的group）
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            
            // 创建新的ServerBootstrap实例，避免group冲突
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(io.netty.channel.socket.nio.NioServerSocketChannel.class)
                    .option(io.netty.channel.ChannelOption.SO_BACKLOG, 128)
                    .childOption(io.netty.channel.ChannelOption.TCP_NODELAY, true)
                    .childOption(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new cn.gp.smartparking.websocket.config.WebSocketChannelInitializer(
                            websocketPath, applicationContext));
            
            // 启动服务器
            channelFuture = bootstrap.bind(port).sync();
            log.info("Netty WebSocket 服务器启动成功，端口: {}, 路径: {}", port, websocketPath);
            log.info("WebSocket 连接地址: ws://localhost:{}/{}", port, websocketPath);
        } catch (Exception e) {
            log.error("Netty WebSocket 服务器启动失败", e);
            shutdown();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channelFuture != null) {
            channelFuture.channel().close().syncUninterruptibly();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        log.info("Netty WebSocket 服务器已关闭");
    }
}
