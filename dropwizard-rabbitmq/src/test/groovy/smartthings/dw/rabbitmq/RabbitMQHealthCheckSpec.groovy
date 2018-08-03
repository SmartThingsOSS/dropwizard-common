package smartthings.dw.rabbitmq

import com.codahale.metrics.health.HealthCheck
import com.rabbitmq.client.Channel
import spock.lang.Specification

class RabbitMQHealthCheckSpec extends Specification {

    Channel channel
    RabbitMQHealthCheck rabbitMQHealthCheck

    def setup() {
        channel = Mock(Channel)
        rabbitMQHealthCheck = new RabbitMQHealthCheck(channel)
    }

    def "Should report unhealthy if channel is closed"() {
        when:
        HealthCheck.Result result = rabbitMQHealthCheck.check()

        then:
        1 * channel.isOpen() >> false
        0 * _

        and:
        !result.healthy
        result.message
        !result.error
    }

    def "Should report healthy if channel is open"() {
        when:
        HealthCheck.Result result = rabbitMQHealthCheck.check()

        then:
        1 * channel.isOpen() >> true
        0 * _

        and:
        result.healthy
        !result.message
        !result.error
    }

    def "Should be named"() {
        when:
        String name = rabbitMQHealthCheck.getName()

        then:
        name == "rabbitmq"
    }
}
