package smartthings.dw.rabbitmq;

import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import smartthings.dw.guice.AbstractDwModule;
import smartthings.dw.rabbitmq.connection.ManagedRabbitMQConnection;

import java.io.IOException;

public class RabbitMQModule extends AbstractDwModule {
    @Override
    protected void configure() {
        requireBinding(RabbitMQConfiguration.class);
        registerManaged(ManagedRabbitMQConnection.class);

        registerHealthCheck(RabbitMQHealthCheck.class);
        bind(RabbitMQHealthCheck.class).in(Scopes.SINGLETON);
    }

    @Provides
    Channel provideChannel(ManagedRabbitMQConnection managedRabbitMQConnection) throws IOException {
        return managedRabbitMQConnection.createChannel();
    }

    @Provides
    ConnectionFactory provideConnectionFactory(RabbitMQConfiguration rabbitMQConfiguration) {
        return rabbitMQConfiguration.buildConnectionFactory();
    }
}
