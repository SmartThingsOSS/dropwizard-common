package smartthings.dw.rabbitmq.connection;

import com.rabbitmq.client.Channel;

import java.io.IOException;

public interface RabbitMQConnection {
    Channel createChannel() throws IOException;
}
