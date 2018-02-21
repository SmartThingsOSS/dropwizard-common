package smartthings.dw.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.NamedHealthCheck;

import javax.inject.Inject;

public class CassandraHealthCheck extends NamedHealthCheck {
	private final static Logger LOG = LoggerFactory.getLogger(CassandraHealthCheck.class);
	private final Session session;
	private final SimpleStatement simpleStatement;

	@Inject
	public CassandraHealthCheck(Session session, CassandraConfiguration config) {
		this.session = session;
		this.simpleStatement = new SimpleStatement(config.getValidationQuery());
		this.simpleStatement.setIdempotent(config.getValidationQueryIdempotence());
	}

	@Override
	public String getName() {
		return "cassandra";
	}

	@Override
	protected Result check() throws Exception {
		try {
			session.execute(simpleStatement);
			return Result.healthy();
		} catch (Exception e) {
			LOG.error("Cassandra health check error", e);
			return Result.unhealthy("Error connecting to cassandra");
		}
	}
}
