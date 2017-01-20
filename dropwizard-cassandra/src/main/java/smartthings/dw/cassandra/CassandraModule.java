package smartthings.dw.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import smartthings.dw.guice.AbstractDwModule;

public class CassandraModule extends AbstractDwModule {

	@Override
	protected void configure() {
	    requireBinding(CassandraConfiguration.class);
		bind(CassandraHealthCheck.class).in(Scopes.SINGLETON);

		registerHealthCheck(CassandraHealthCheck.class);
		registerManaged(CassandraManaged.class);
	}

	@Provides
	@Singleton
	Session provideSession(CassandraConfiguration config) {
		return config.buildSession();
	}

	@Provides
	@Singleton
	MappingManager provideMappingManager(Session session) {
		return new MappingManager(session);
	}

	@Provides
    Cluster provideCluster(Session session) { return session.getCluster(); }
}
