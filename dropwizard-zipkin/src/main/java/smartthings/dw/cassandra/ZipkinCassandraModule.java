package smartthings.dw.cassandra;

import brave.Tracing;
import brave.cassandra.driver.CassandraClientTracing;
import brave.cassandra.driver.TracingSession;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.TracedMappingManager;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import smartthings.brave.cassandra.driver.NamedCassandraClientParser;
import smartthings.dw.guice.AbstractDwModule;

/**
 * ZipkinCassandraModule does not extend smartthings.dw.cassandra.CassandraModule because
 * com.google.inject.Provides cannot be overridden at the module sub-class
 */
public class ZipkinCassandraModule extends AbstractDwModule {

    private final Session baseSession;

    public ZipkinCassandraModule(CassandraConfiguration config) {
        baseSession = config.buildSession();
    }

    @Override
    protected void configure() {
        bind(CassandraHealthCheck.class).in(Scopes.SINGLETON);
        registerHealthCheck(CassandraHealthCheck.class);
    }

    @Provides
    @Singleton
    Session provideTracedSession(CassandraClientTracing cassandraClientTracing) {
        return TracingSession.create(cassandraClientTracing, baseSession);
    }

    @Provides
    @Singleton
    MappingManager provideMappingManager(Session session) {
        return new TracedMappingManager(session);
    }

    @Provides
    @Singleton
    CassandraClientTracing provideCassandraClientTracing(Tracing tracing) {
        return CassandraClientTracing
            .newBuilder(tracing)
            .parser(new NamedCassandraClientParser())
            .build();
    }
}
