package cn.gp.smartparking.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component; /**
 * WebSocket 配置属性类
 */
@Component
@ConfigurationProperties(prefix = "websocket")
public class WebSocketProperties {
    private int port = 8081;
    private String path = "/websocket";
    private Heartbeat heartbeat = new Heartbeat();
    private Connection connection = new Connection();
    private Message message = new Message();

    // Getters and Setters
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Heartbeat getHeartbeat() { return heartbeat; }
    public void setHeartbeat(Heartbeat heartbeat) { this.heartbeat = heartbeat; }

    public Connection getConnection() { return connection; }
    public void setConnection(Connection connection) { this.connection = connection; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public static class Heartbeat {
        private int interval = 30000;
        private int timeout = 90000;

        public int getInterval() { return interval; }
        public void setInterval(int interval) { this.interval = interval; }

        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }

    public static class Connection {
        private int maxConnections = 10000;
        private int idleTime = 300;

        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public int getIdleTime() { return idleTime; }
        public void setIdleTime(int idleTime) { this.idleTime = idleTime; }
    }

    public static class Message {
        private int maxSize = 65536;
        private int timeout = 5000;

        public int getMaxSize() { return maxSize; }
        public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
}
