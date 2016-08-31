package smartthings.dw.cassandra.loadbalancing;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("dcAwareRoundRobin")
public class DCAwareRoundRobinPolicyFactory implements LoadBalancingPolicyFactory {
	private final static Logger LOG = LoggerFactory.getLogger(DCAwareRoundRobinPolicyFactory.class);

	private String localDc;
	private Integer usedHostsPerRemoteDC;
	private Boolean allowRemoteDCsForLocalConsistencyLevel = false;

	@JsonProperty
	public String getLocalDc() {
		return localDc;
	}

	@JsonProperty
	public void setLocalDc(String localDc) {
		this.localDc = localDc;
	}

	@JsonProperty
	public Integer getUsedHostsPerRemoteDC() {
		return usedHostsPerRemoteDC;
	}

	@JsonProperty
	public void setUsedHostsPerRemoteDC(Integer usedHostsPerRemoteDC) {
		this.usedHostsPerRemoteDC = usedHostsPerRemoteDC;
	}

	@JsonProperty
	public Boolean getAllowRemoteDCsForLocalConsistencyLevel() {
		return allowRemoteDCsForLocalConsistencyLevel;
	}

	@JsonProperty
	public void setAllowRemoteDCsForLocalConsistencyLevel(Boolean allowRemoteDCsForLocalConsistencyLevel) {
		this.allowRemoteDCsForLocalConsistencyLevel = allowRemoteDCsForLocalConsistencyLevel;
	}

	@Override
	public LoadBalancingPolicy build() {
		LOG.info("DCAwareRoundRobinPolicy");
		DCAwareRoundRobinPolicy.Builder builder = DCAwareRoundRobinPolicy.builder();
		if (allowRemoteDCsForLocalConsistencyLevel) {
			LOG.info("DCAwareRoundRobinPolicy - allowRemoteDCsForLocalConsistencyLevel true");
			builder.allowRemoteDCsForLocalConsistencyLevel();
		}
		if (usedHostsPerRemoteDC != null) {
			LOG.info("DCAwareRoundRobinPolicy - usedHostsPerRemoteDC {}", usedHostsPerRemoteDC);
			builder.withUsedHostsPerRemoteDc(usedHostsPerRemoteDC);
		}
		if (localDc != null) {
			LOG.info("DCAwareRoundRobinPolicy - localDc {}", localDc);
			builder.withLocalDc(localDc);
		}
		return builder.build();
	}
}
