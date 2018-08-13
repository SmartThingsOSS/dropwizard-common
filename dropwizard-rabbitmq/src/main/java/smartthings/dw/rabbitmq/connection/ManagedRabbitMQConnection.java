package smartthings.dw.rabbitmq.connection;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.rabbitmq.RabbitMQConfiguration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


@Singleton
public class ManagedRabbitMQConnection implements RabbitMQConnection, Managed {

    private static final Logger log = LoggerFactory.getLogger(ManagedRabbitMQConnection.class);
    private final Connection connection;
    private final RabbitMQConfiguration config;

    @Inject
    public ManagedRabbitMQConnection(ConnectionFactory connectionFactory, RabbitMQConfiguration config) throws IOException, TimeoutException {
        this.connection = connectionFactory.newConnection();
        this.config = config;
    }

    @Override
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
