package smartthings.dw.rabbitmq

import com.codahale.metrics.health.HealthCheck
import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.TypeLiteral
import com.google.inject.util.Modules
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.dropwizard.lifecycle.Managed
import smartthings.dw.rabbitmq.connection.ManagedRabbitMQConnection
import spock.lang.Specification

class RabbitMQModuleSpec extends Specification {
    RabbitMQConfiguration rabbitMQConfiguration
    Channel channel
    Connection connection
    ConnectionFactory connectionFactory

    private static class StubbedModule implements Module {
        private final RabbitMQConfiguration rabbitMQConfiguration
        private final ConnectionFactory connectionFactory
        private final Channel channel

        StubbedModule(ConnectionFactory connectionFactory, Channel channel, RabbitMQConfiguration rabbitMQConfiguration) {
            this.rabbitMQConfiguration = rabbitMQConfiguration
            this.connectionFactory = connectionFactory
            this.channel = channel
        }

        @Override
        void configure(Binder binder) {
            binder.bind(RabbitMQConfiguration).toInstance(rabbitMQConfiguration)
            binder.bind(ConnectionFactory).toInstance(connectionFactory)
            binder.bind(Channel).toInstance(channel)
        }
    }

    def setup() {
        rabbitMQConfiguration = Mock(RabbitMQConfiguration)
        channel = Mock(Channel)
        connection = Mock(Connection)
        connectionFactory = new StubbedConnectionFactory(connection)
    }

    def 'Registers RabbitMQ health check'() {
        given:
        Injector injector = Guice.createInjector(Modules.override(new RabbitMQModule()).with(new StubbedModule(connectionFactory, channel, rabbitMQConfiguration)))

        when:
        List healthChecksBindings = injector.findBindingsByType(new TypeLiteral<Set<HealthCheck>>() {})

        then:
        0 * _

        and:
        healthChecksBindings.size() == 1
        Set checks = healthChecksBindings.first().provider.get()
        checks.size() == 1
        checks.find { it instanceof RabbitMQHealthCheck }
    }

    def 'Registers a managed connection to RabbbitMQ'() {
        given:
        Injector injector = Guice.createInjector(Modules.override(new RabbitMQModule()).with(new StubbedModule(connectionFactory, channel, rabbitMQConfiguration)))

        when:
        List managedBindings = injector.findBindingsByType(new TypeLiteral<Set<Managed>>() {})

        then:
        0 * _

        and:
        managedBindings.size() == 1
        Set managed  = managedBindings.first().provider.get()
        managed.size() == 1
        managed.find { it instanceof ManagedRabbitMQConnection }
    }

    def 'Provides a channel'() {
        given:
        RabbitMQModule rabbitMQModule = new RabbitMQModule()
        ManagedRabbitMQConnection managedConnection = Mock(ManagedRabbitMQConnection)

        when:
        Channel providedChannel = rabbitMQModule.provideChannel(managedConnection)

        then:
        1 * managedConnection.createChannel() >> channel

        and:
        providedChannel == channel
    }

    def 'Provides a connection factory'() {
        given:
        RabbitMQModule rabbitMQModule = new RabbitMQModule()

        when:
        ConnectionFactory providedConnectionFactory = rabbitMQModule.provideConnectionFactory(rabbitMQConfiguration)

        then:
        1 * rabbitMQConfiguration.buildConnectionFactory() >> connectionFactory

        and:
        providedConnectionFactory == connectionFactory
    }
}
