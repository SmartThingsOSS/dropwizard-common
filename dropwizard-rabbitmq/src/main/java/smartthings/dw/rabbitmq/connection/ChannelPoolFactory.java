package smartthings.dw.rabbitmq.connection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelPoolFactory implements PooledObjectFactory<Channel> {

    private final Connection connection;

    public ChannelPoolFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public PooledObject<Channel> makeObject() throws Exception {
        return new DefaultPooledObject<>(connection.createChannel());
    }

    @Override
    public void destroyObject(PooledObject<Channel> pooledChannel) throws Exception {
        pooledChannel.getObject().close();
    }

    @Override
    public boolean validateObject(PooledObject<Channel> pooledChannel) {
        return pooledChannel.getObject().isOpen();
    }

    @Override
    public void activateObject(PooledObject<Channel> pooledChannel) throws Exception {
        return;
    }

    @Override
    public void passivateObject(PooledObject<Channel> pooledChannel) throws Exception {
        return;
    }
}
