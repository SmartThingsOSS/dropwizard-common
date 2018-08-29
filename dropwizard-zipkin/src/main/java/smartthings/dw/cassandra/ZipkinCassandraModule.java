package smartthings.dw.cassandra;

import brave.Tracing;
import brave.cassandra.driver.CassandraClientSampler;
import brave.cassandra.driver.CassandraClientTracing;
import brave.cassandra.driver.TracingSession;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
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

    private final CassandraConfiguration config;
    private final Session baseSession;
    private final CassandraClientSampler clientSampler;

    public ZipkinCassandraModule(CassandraConfiguration config) {
        this(config, null);
    }

    public ZipkinCassandraModule(CassandraConfiguration config, CassandraClientSampler clientSampler) {
        this.config = config;
        baseSession = config.buildSession();

        if (clientSampler != null) {
            this.clientSampler = clientSampler;
        } else{
            this.clientSampler = new IgnoreHealthCheckSampler(config);
        }
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
        CassandraClientTracing.Builder builder = CassandraClientTracing
            .newBuilder(tracing)
            .parser(new NamedCassandraClientParser());

        if (clientSampler != null) {
            builder.sampler(clientSampler);
        }

        return builder.build();
    }

    private static class IgnoreHealthCheckSampler extends CassandraClientSampler {
        private final CassandraConfiguration config;

        IgnoreHealthCheckSampler(CassandraConfiguration config) {
            this.config = config;
        }

        @Override
        public Boolean trySample(Statement statement) {
            if (statement instanceof BoundStatement) {
                return !((BoundStatement)statement).preparedStatement().getQueryString().equals(
                    config.getValidationQuery().getQueryString());
            }

            return true;
        }
    }
}
