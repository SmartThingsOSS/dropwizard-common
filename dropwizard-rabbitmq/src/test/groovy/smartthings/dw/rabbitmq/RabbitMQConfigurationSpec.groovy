package smartthings.dw.rabbitmq

import com.rabbitmq.client.ConnectionFactory
import spock.lang.Specification

class RabbitMQConfigurationSpec extends Specification {
    RabbitMQConfiguration rabbitMQConfiguration

    def setup() {
        rabbitMQConfiguration = new RabbitMQConfiguration()
    }

    def "should be able to create a connection factory with default configuration"() {
        given:
        ConnectionFactory defaultConnectionFactory = new ConnectionFactory()

        when:
        rabbitMQConfiguration.setHost(host)
        rabbitMQConfiguration.setUsername(username)
        rabbitMQConfiguration.setPassword(password)

        and:
        ConnectionFactory connectionFactory = rabbitMQConfiguration.buildConnectionFactory()

        then:
        connectionFactory.username == username
        connectionFactory.password == password
        connectionFactory.host == host

        connectionFactory.automaticRecoveryEnabled
        connectionFactory.virtualHost == "/"

        connectionFactory.port == defaultConnectionFactory.port
        connectionFactory.port == 5672
        connectionFactory.connectionTimeout == defaultConnectionFactory.connectionTimeout
        connectionFactory.connectionTimeout == 60000
        connectionFactory.networkRecoveryInterval == defaultConnectionFactory.networkRecoveryInterval
        connectionFactory.networkRecoveryInterval == 5000

        where:
        host        | username | password
        "localhost" | "test"   | "shitsasecret"
    }

    def "Should be able to create a connection factory with overrides"() {
        when:
        rabbitMQConfiguration.setHost(host)
        rabbitMQConfiguration.setVirtualHost(virtualHost)
        rabbitMQConfiguration.setPort(port)
        rabbitMQConfiguration.setUsername(username)
        rabbitMQConfiguration.setPassword(password)
        rabbitMQConfiguration.setAutomaticRecoveryEnabled(automaticRecoveryEnabled)
        rabbitMQConfiguration.setConnectionTimeout(connectionTimeout)
        rabbitMQConfiguration.setNetworkRecoveryInterval(networkRecoveryInterval)

        and:
        ConnectionFactory connectionFactory = rabbitMQConfiguration.buildConnectionFactory()

        then:
        connectionFactory.username == username
        connectionFactory.password == password
        connectionFactory.host == host
        connectionFactory.virtualHost == virtualHost
        connectionFactory.port == port
        connectionFactory.automaticRecoveryEnabled == automaticRecoveryEnabled
        connectionFactory.connectionTimeout == connectionTimeout
        connectionFactory.networkRecoveryInterval == networkRecoveryInterval

        where:
        host        | virtualHost | port | username | password        | automaticRecoveryEnabled | connectionTimeout | networkRecoveryInterval
        "localhost" | "rabbit"    | 8080 | "test"   | "shitsitsecret" | false                    | 1000              | 2
    }

    def "Should be able to set the shutdown timeout for stopping the managed connection"() {
        given:
        int shutdownTimeout = 2

        when:
        int configuredShutdownTimeout = rabbitMQConfiguration.getShutdownTimeout()

        then:
        configuredShutdownTimeout == 2000

        when:
        rabbitMQConfiguration.setShutdownTimeout(shutdownTimeout)
        int updatedShutdownTimeout = rabbitMQConfiguration.getShutdownTimeout()

        then:
        updatedShutdownTimeout == shutdownTimeout
    }
}
