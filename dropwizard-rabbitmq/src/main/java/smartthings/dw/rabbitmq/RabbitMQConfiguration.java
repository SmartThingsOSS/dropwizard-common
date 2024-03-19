package smartthings.dw.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import org.hibernate.validator.constraints.NotEmpty;

import static com.rabbitmq.client.ConnectionFactory.DEFAULT_HEARTBEAT;

public class RabbitMQConfiguration {

    @NotEmpty
    private String host;

    @NotEmpty
    private String virtualHost = "/";

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private Integer port = null;

    private boolean automaticRecoveryEnabled = true;

    private Integer connectionTimeout = 60000;

    private int shutdownTimeout = 2000;

    private Long networkRecoveryInterval = null;

    public int getShutdownTimeout() {
        return shutdownTimeout;
    }

    private boolean useNio = false;

    private int requestedHeartBeatTimeoutInSecs = DEFAULT_HEARTBEAT;

    ConnectionFactory buildConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(host);
        factory.setVirtualHost(virtualHost);
        if (port != null) {
            factory.setPort(port);
        }

        factory.setUsername(username);
        factory.setPassword(password);

        factory.setConnectionTimeout(connectionTimeout);
        factory.setAutomaticRecoveryEnabled(automaticRecoveryEnabled);
        if (networkRecoveryInterval != null) {
            factory.setNetworkRecoveryInterval(networkRecoveryInterval);
        }

        if(useNio) {
            factory.useNio();
        }

        factory.setRequestedHeartbeat(requestedHeartBeatTimeoutInSecs);

        return factory;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setAutomaticRecoveryEnabled(boolean automaticRecoveryEnabled) {
        this.automaticRecoveryEnabled = automaticRecoveryEnabled;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setShutdownTimeout(int shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    public void setNetworkRecoveryInterval(Long networkRecoveryInterval) {
        this.networkRecoveryInterval = networkRecoveryInterval;
    }

    public boolean isUseNio() {
        return useNio;
    }

    public void setUseNio(boolean useNio) {
        this.useNio = useNio;
    }

    public int getRequestedHeartBeatTimeoutInSecs() {
        return requestedHeartBeatTimeoutInSecs;
    }

    public void setRequestedHeartBeatTimeoutInSecs(int requestedHeartBeatTimeoutInSecs) {
        this.requestedHeartBeatTimeoutInSecs = requestedHeartBeatTimeoutInSecs;
    }
}
