package smartthings.dw.cassandra.loadbalancing;

import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("roundRobin")
public class RoundRobinPolicyFactory implements LoadBalancingPolicyFactory {
	private final static Logger LOG = LoggerFactory.getLogger(RoundRobinPolicyFactory.class);

	@Override
	public LoadBalancingPolicy build() {
		LOG.info("RoundRobinPolicy");
		return new RoundRobinPolicy();
	}
}
