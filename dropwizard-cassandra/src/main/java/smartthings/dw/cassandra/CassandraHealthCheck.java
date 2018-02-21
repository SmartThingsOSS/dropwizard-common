package smartthings.dw.cassandra;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.NamedHealthCheck;

import javax.inject.Inject;

public class CassandraHealthCheck extends NamedHealthCheck {
	private final static Logger LOG = LoggerFactory.getLogger(CassandraHealthCheck.class);
	private final Session session;
	private final PreparedStatement validationQuery;

	@Inject
	public CassandraHealthCheck(Session session, CassandraConfiguration config) {
		this.session = session;
		this.validationQuery = session.prepare(config.getValidationQuery());
	}

	@Override
	public String getName() {
		return "cassandra";
	}

	@Override
	protected Result check() throws Exception {
		try {
			session.execute(validationQuery.bind());
			return Result.healthy();
		} catch (Exception e) {
			LOG.error("Cassandra health check error", e);
			return Result.unhealthy("Error connecting to cassandra");
		}
	}
}
