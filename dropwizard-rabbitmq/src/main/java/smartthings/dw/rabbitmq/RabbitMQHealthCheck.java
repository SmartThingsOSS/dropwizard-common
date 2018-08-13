package smartthings.dw.rabbitmq;

import com.rabbitmq.client.Channel;
import smartthings.dw.guice.NamedHealthCheck;

import javax.inject.Inject;

public class RabbitMQHealthCheck extends NamedHealthCheck {
    private final Channel channel;

    @Inject
    public RabbitMQHealthCheck(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String getName() {
        return "rabbitmq";
    }

    @Override
    protected Result check() {
        if (channel.isOpen()) {
            return Result.healthy();
        } else {
            return Result.unhealthy("Closed channel");
        }
    }
}
