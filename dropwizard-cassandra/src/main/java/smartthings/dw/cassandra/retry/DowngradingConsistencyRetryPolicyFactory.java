package smartthings.dw.cassandra.retry;

import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("downgradingConsistencyRetry")
public class DowngradingConsistencyRetryPolicyFactory implements RetryPolicyFactory {
	private final static Logger LOG = LoggerFactory.getLogger(DowngradingConsistencyRetryPolicyFactory.class);

	@Override
	public RetryPolicy build() {
		LOG.info("DowngradingConsistencyRetry - getting instance");
		return DowngradingConsistencyRetryPolicy.INSTANCE;
	}
}
