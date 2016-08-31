package smartthings.dw.datadog;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingRetryListener implements RetryListener {
	private final static Logger LOG = LoggerFactory.getLogger(LoggingRetryListener.class);
	private final String msg;
	private final int maxAttempts;

	public LoggingRetryListener(String msg, int maxAttempts) {
		Preconditions.checkNotNull(msg);
		this.msg = msg;
		this.maxAttempts = maxAttempts;
	}

	@Override
	public <V> void onRetry(Attempt<V> attempt) {
		long attemptCount = attempt.getAttemptNumber();
		if (attempt.hasException() && maxAttempts > attemptCount) {
			LOG.warn("Error found in retry loop while {}. Attempt {}.",
					msg, attempt.getAttemptNumber(), attempt.getExceptionCause());
		}
	}
}
