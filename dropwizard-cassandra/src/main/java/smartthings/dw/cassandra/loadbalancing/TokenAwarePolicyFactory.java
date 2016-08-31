package smartthings.dw.cassandra.loadbalancing;

import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@JsonTypeName("tokenAware")
public class TokenAwarePolicyFactory implements LoadBalancingPolicyFactory {
	private final static Logger LOG = LoggerFactory.getLogger(TokenAwarePolicyFactory.class);

	@Valid
	@NotNull
	private LoadBalancingPolicyFactory subPolicy;

	private Boolean shuffleReplicas;

	@JsonProperty
	public LoadBalancingPolicyFactory getSubPolicy() {
		return subPolicy;
	}

	@JsonProperty
	public void setSubPolicy(LoadBalancingPolicyFactory subPolicy) {
		this.subPolicy = subPolicy;
	}

	@JsonProperty
	public Boolean getShuffleReplicas() {
		return shuffleReplicas;
	}

	@JsonProperty
	public void setShuffleReplicas(Boolean shuffleReplicas) {
		this.shuffleReplicas = shuffleReplicas;
	}

	@Override
	public LoadBalancingPolicy build() {
		if (shuffleReplicas == null) {
			LOG.info("TokenAwarePolicyFactory - subPolicy {}", subPolicy.getClass().getCanonicalName());
			return new TokenAwarePolicy(subPolicy.build());
		} else {
			LOG.info("TokenAwarePolicyFactory - subPolicy {}", subPolicy.getClass().getCanonicalName());
			LOG.info("TokenAwarePolicyFactory - shuffleReplicas {}", shuffleReplicas);
			return new TokenAwarePolicy(subPolicy.build(), shuffleReplicas);
		}
	}
}
