package smartthings.dw.cassandra.speculative;

import com.datastax.driver.core.policies.PercentileSpeculativeExecutionPolicy;
import com.datastax.driver.core.policies.SpeculativeExecutionPolicy;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.cassandra.speculative.percentile.PerHostPercentileTrackerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonTypeName("percentile")
public class PercentileSpeculativeExecutionPolicyFactory implements SpeculativeExecutionPolicyFactory {
	private final static Logger LOG = LoggerFactory.getLogger(PercentileSpeculativeExecutionPolicyFactory.class);

	@NotNull
	private Double percentile;

	@NotNull
	private Integer maxSpeculativeExecutions;

	@NotNull
	@Valid
	private PerHostPercentileTrackerFactory tracker = new PerHostPercentileTrackerFactory();

	@Override
	public SpeculativeExecutionPolicy build() {
		LOG.info("SpeculativeExecutionPolicy - percentile {}", percentile);
		LOG.info("SpeculativeExecutionPolicy - maxSpeculativeExecutions {}", maxSpeculativeExecutions);
		return new PercentileSpeculativeExecutionPolicy(tracker.build(), percentile, maxSpeculativeExecutions);
	}

	public Double getPercentile() {
		return percentile;
	}

	public void setPercentile(Double percentile) {
		this.percentile = percentile;
	}

	public Integer getMaxSpeculativeExecutions() {
		return maxSpeculativeExecutions;
	}

	public void setMaxSpeculativeExecutions(Integer maxSpeculativeExecutions) {
		this.maxSpeculativeExecutions = maxSpeculativeExecutions;
	}

	public PerHostPercentileTrackerFactory getTracker() {
		return tracker;
	}

	public void setTracker(PerHostPercentileTrackerFactory tracker) {
		this.tracker = tracker;
	}
}
