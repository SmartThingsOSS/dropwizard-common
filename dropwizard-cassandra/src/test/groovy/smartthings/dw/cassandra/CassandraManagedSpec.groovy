package smartthings.dw.cassandra

import com.datastax.driver.core.CloseFuture
import com.datastax.driver.core.Cluster
import spock.lang.Specification

import java.util.concurrent.TimeUnit

class CassandraManagedSpec extends Specification {
    Cluster cluster
    CassandraConfiguration config
    Long shutdownTimeoutMillis = 3000

    def setup() {
        cluster = Mock(Cluster)
        config = Mock(CassandraConfiguration)
        config.getShutdownTimeoutInMillis() >> shutdownTimeoutMillis
    }

    def 'init cluster on start'() {
        given:
        def managed = new CassandraManaged(config, cluster)

        when:
        managed.start()

        then:
        1 * cluster.init() >> cluster
    }

    def 'close cluster on stop'() {
        given:
        CloseFuture future = Mock(CloseFuture)
        def managed = new CassandraManaged(config, cluster)

        when:
        managed.stop()

        then:
        1 * cluster.closeAsync() >> future
        1 * future.get(shutdownTimeoutMillis, TimeUnit.MILLISECONDS)
    }
}
