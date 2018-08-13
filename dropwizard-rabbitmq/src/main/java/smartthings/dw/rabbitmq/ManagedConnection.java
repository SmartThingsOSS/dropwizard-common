package smartthings.dw.rabbitmq;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


@Singleton
public class ManagedConnection implements Managed {

    private static final Logger log = LoggerFactory.getLogger(ManagedConnection.class);
    private final Connection connection;
    private final RabbitMQConfiguration config;

    @Inject
    public ManagedConnection(ConnectionFactory connectionFactory, RabbitMQConfiguration config) throws IOException, TimeoutException {
        this.connection = connectionFactory.newConnection();
        this.config = config;
    }

    public Channel createChannel() throws IOException {
        return connection.createChannel();
    }

    @Override
    public void start() {
        log.trace("Managed connection started");
    }

    @Override
    public void stop() {
        try {
            if (connection.isOpen()) {
                connection.close(config.getShutdownTimeout());
                log.info("RabbitMQ Connection Closed");
            }
        } catch (Exception e) {
            log.error("Can't Close RabbitMQ Producer Connection Cleanly", e);
            connection.abort(config.getShutdownTimeout());
            log.info("RabbitMQ Connection Aborted");
        }
    }
}
