package smartthings.dw.cassandra.speculative.percentile;

import com.datastax.driver.core.PerHostPercentileTracker;
import com.datastax.driver.core.SocketOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

public class PerHostPercentileTrackerFactory {
	private final static Logger LOG = LoggerFactory.getLogger(PerHostPercentileTrackerFactory.class);

	@NotNull
	private Integer highestTrackableLatencyMillis = SocketOptions.DEFAULT_READ_TIMEOUT_MILLIS + 500;

	public Integer getHighestTrackableLatencyMillis() {
		return highestTrackableLatencyMillis;
	}

	public void setHighestTrackableLatencyMillis(Integer highestTrackableLatencyMillis) {
		this.highestTrackableLatencyMillis = highestTrackableLatencyMillis;
	}

	public PerHostPercentileTracker build() {
		LOG.info("PerHostPercentileTracker - highestTrackableLatencyMillis {}", highestTrackableLatencyMillis);
		return PerHostPercentileTracker.builder(highestTrackableLatencyMillis).build();
	}
}
