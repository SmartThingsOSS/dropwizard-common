package smartthings.dw.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.TracedMappingManager;
import com.github.kristofa.brave.Brave;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import smartthings.brave.cassandra.TracedSession;
import smartthings.dw.guice.AbstractDwModule;
import smartthings.dw.zipkin.ZipkinConfiguration;

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
    Session provideTracedSession(ZipkinConfiguration zipkinConfiguration, Brave brave) {
        return TracedSession.create(baseSession, brave, zipkinConfiguration.getServiceName());
    }

    @Provides
    @Singleton
    MappingManager provideMappingManager(Session session) {
        return new TracedMappingManager(session);
    }
}
