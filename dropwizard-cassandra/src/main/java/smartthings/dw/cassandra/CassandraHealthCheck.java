package smartthings.dw.cassandra;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.NamedHealthCheck;

import javax.inject.Inject;

public class CassandraHealthCheck extends NamedHealthCheck {
	private final static Logger LOG = LoggerFactory.getLogger(CassandraHealthCheck.class);
	private final Session session;
	private final String validationQuery;
	private final boolean validationQueryIdempotence;

	@Inject
	public CassandraHealthCheck(Session session, CassandraConfiguration config) {
		this.session = session;
		this.validationQuery = config.getValidationQuery();
		this.validationQueryIdempotence = config.getValidationQueryIdempotence();
	}

	@Override
	public String getName() {
		return "cassandra";
	}

	@Override
	protected Result check() throws Exception {
		try {
			session.execute(session.prepare(validationQuery)
                .setIdempotent(validationQueryIdempotence).bind());
			return Result.healthy();
		} catch (Exception e) {
			LOG.error("Cassandra health check error", e);
			return Result.unhealthy("Error connecting to cassandra");
		}
	}
}
