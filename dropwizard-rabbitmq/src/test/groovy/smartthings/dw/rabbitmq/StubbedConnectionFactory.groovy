package smartthings.dw.rabbitmq

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class StubbedConnectionFactory extends ConnectionFactory {
    private Connection connection

    StubbedConnectionFactory(Connection connection) {
        this.connection = connection
    }

    @Override
    Connection newConnection() {
        this.connection
    }

    void setConnection(Connection connection) {
        this.connection = connection
    }
}
