package smartthings.dw.rabbitmq

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import smartthings.dw.rabbitmq.connection.ManagedRabbitMQConnection
import spock.lang.Specification

import java.util.concurrent.TimeoutException


class ManagedRabbitMQConnectionSpec extends Specification {

    RabbitMQConfiguration rabbitMQConfiguration
    StubbedConnectionFactory connectionFactory
    Connection connection

    ManagedRabbitMQConnection managedConnection

    def setup() {
        rabbitMQConfiguration = Mock(RabbitMQConfiguration)
        connection = Mock(Connection)
        connectionFactory = new StubbedConnectionFactory(connection)
        managedConnection = new ManagedRabbitMQConnection(connectionFactory, rabbitMQConfiguration)
    }

    def "Should be able to create a channel"() {
        given:
        Channel mockedChannel = Mock(Channel)

        when:
        Channel createdChannel = managedConnection.createChannel()

        then:
        1 * connection.createChannel() >> mockedChannel
        0 * _

        and:
        mockedChannel == createdChannel
    }

    def "Should create connection on instantiation not start"() {
        given:
        Connection newConnection = Mock(Connection)
        connectionFactory.setConnection(newConnection)
        Connection originalConnection = managedConnection.connection

        when:
        managedConnection.start()

        then:
        originalConnection == managedConnection.connection
        newConnection != managedConnection.connection
        0 * _

        when:
        managedConnection = new ManagedRabbitMQConnection(connectionFactory, rabbitMQConfiguration)

        then:
        originalConnection != managedConnection.connection
        newConnection == managedConnection.connection
        0 * _
    }

    def "Should do nothing if connection is already closed on shutdown"() {
        when:
        managedConnection.stop()

        then:
        1 * connection.isOpen() >> false
        0 * _
    }

    def "Should close the connection cleanly if open on shutdown"() {
        given:
        int shutdownTimeout = 2000

        when:
        managedConnection.stop()

        then:
        1 * connection.isOpen() >> true
        1 * rabbitMQConfiguration.getShutdownTimeout() >> shutdownTimeout
        1 * connection.close(shutdownTimeout)
        0 * _
    }

    def "Should abort the connection if exceptions are thrown attempting to close it cleanly on shutdown"() {
        given:
        int shutdownTimeout = 2000

        when:
        managedConnection.stop()

        then:
        1 * connection.isOpen() >> true
        1 * rabbitMQConfiguration.getShutdownTimeout() >> shutdownTimeout
        1 * connection.close(shutdownTimeout) >> { args ->
            throw new TimeoutException()
        }
        1 * rabbitMQConfiguration.getShutdownTimeout() >> shutdownTimeout
        1 * connection.abort(shutdownTimeout)
        0 * _
    }

    def "Should borrow channel from pool"() {
        given: "a mock channel"
        Channel channel = Mock(Channel)

        when: "getting a channel the first time"
        Channel chan = managedConnection.getChannel()

        then: "it should create a channel"
        1 * connection.createChannel() >> channel

        when: "returning the channel and borrowing again"
        managedConnection.returnChannel(channel)

        then: "it should not create another channel"
        0 * connection.createChannel()
    }
}
