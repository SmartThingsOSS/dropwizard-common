package smartthings.dw.cassandra.retry;

import com.datastax.driver.core.policies.LoggingRetryPolicy;
import com.datastax.driver.core.policies.RetryPolicy;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonTypeName("loggingRetry")
public class LoggingRetryPolicyFactory implements RetryPolicyFactory {
	private final static Logger LOG = LoggerFactory.getLogger(LoggingRetryPolicyFactory.class);

	@NotNull
	@Valid
	private RetryPolicyFactory subPolicy;

	@Override
	public RetryPolicy build() {
		RetryPolicy retryPolicy = subPolicy.build();
		LOG.info("LoggingRetryPolicy - wrapping policy of class {}", retryPolicy.getClass().getCanonicalName());
		return new LoggingRetryPolicy(retryPolicy);
	}

	public RetryPolicyFactory getSubPolicy() {
		return subPolicy;
	}

	public void setSubPolicy(RetryPolicyFactory subPolicy) {
		this.subPolicy = subPolicy;
	}
}
