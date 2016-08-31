package smartthings.dw.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import smartthings.dw.guice.AbstractDwModule;

public class CassandraModule extends AbstractDwModule {

	@Override
	protected void configure() {
		bind(CassandraHealthCheck.class).in(Scopes.SINGLETON);

		registerHealthCheck(CassandraHealthCheck.class);
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
}
