package smartthings.dw.cassandra;

import com.datastax.driver.core.CloseFuture;
import com.datastax.driver.core.Cluster;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CassandraManaged implements Managed {

    private static final Logger logger = LoggerFactory.getLogger(CassandraManaged.class);
    private final Cluster cluster;
    private final Duration shutdownTimeout;

    @Inject
    public CassandraManaged(CassandraConfiguration config, Cluster cluster) {
        this.cluster = cluster;
        this.shutdownTimeout = Duration.milliseconds(config.getShutdownTimeoutInMillis());
    }

    @Override
    public void start() throws Exception {
        cluster.init();
    }

    @Override
    public void stop() throws Exception {
        CloseFuture future = cluster.closeAsync();
        try {
            future.get(shutdownTimeout.toMilliseconds(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            logger.warn("Cassandra cluster timed out to close. Will force to close it");
            future.force();
        }
    }
}
