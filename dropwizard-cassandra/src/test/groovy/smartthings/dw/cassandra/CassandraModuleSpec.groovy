package smartthings.dw.cassandra

import com.codahale.metrics.health.HealthCheck
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Session
import com.datastax.driver.mapping.MappingManager
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.TypeLiteral
import io.dropwizard.lifecycle.Managed
import smartthings.dw.guice.AbstractDwModule
import spock.lang.Specification


class CassandraModuleSpec extends Specification {

    final static String validationQuery = 'validation query'
    final static boolean validationQueryIdempotence = true

    CassandraConfiguration config
    Session session
    MappingManager mappingManager
    Cluster cluster

    private static class DepModule extends AbstractDwModule {
        private final CassandraConfiguration config
        DepModule(CassandraConfiguration config) {
            this.config = config
        }
        @Override
        protected void configure() {
            bind(CassandraConfiguration).toInstance(config)
        }
    }

    def setup() {
        config = Mock(CassandraConfiguration)
        session = Mock(Session)
        mappingManager = Mock(MappingManager)
        cluster = Mock(Cluster)
        config.buildSession() >> session
        config.getShutdownTimeoutInMillis() >> 30000
        config.getValidationQuery() >> validationQuery
        config.getValidationQueryIdempotence() >> validationQueryIdempotence
        session.getCluster() >> cluster
    }

    def 'register cassandra health check'() {
        given:
        Injector injector = Guice.createInjector(new CassandraModule(), new DepModule(config))

        when:
        def checksBindings = injector.findBindingsByType(new TypeLiteral<Set<HealthCheck>>() {})

        then:
        checksBindings.size() > 0
        def checks  = checksBindings.first().provider.get()
        checks.size() > 0
        checks.first() instanceof CassandraHealthCheck
    }

    def 'register cassandra managed'() {
        given:
        Injector injector = Guice.createInjector(new CassandraModule(), new DepModule(config))

        when:
        def managedBindings = injector.findBindingsByType(new TypeLiteral<Set<Managed>>() {})

        then:
        managedBindings.size() > 0
        def setOfManaged  = managedBindings.first().provider.get()
        setOfManaged.size() > 0
        setOfManaged.first() instanceof CassandraManaged
    }
}
