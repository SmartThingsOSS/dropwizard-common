package smartthings.dw.oauth;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class AuthConfiguration {

	@NotEmpty
	private String host;

	@NotEmpty
	private String user;

	@NotEmpty
	private String password;

	@NotNull
	private Duration requestTimeout = Duration.seconds(5);

	@NotNull
	private Duration cacheTTL = Duration.minutes(10);

	@NotNull
	private Boolean enabled = true;

	@NotNull
	private Set<Integer> transparentServerStatusCodes = new HashSet<>();

	@NotNull
	private Integer cacheSize = 100;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Duration getCacheTTL() {
		return cacheTTL;
	}

	public void setCacheTTL(Duration cacheTTL) {
		this.cacheTTL = cacheTTL;
	}

	public Duration getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(Duration requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setTransparentServerStatusCodes(Set<Integer> transparentServerStatusCodes) {
	    this.transparentServerStatusCodes = ImmutableSet.copyOf(transparentServerStatusCodes);
    }

	public Set<Integer> getTransparentServerStatusCodes() {
	    return transparentServerStatusCodes;
    }

    public Integer getCacheSize() {
	    return cacheSize;
	}
}
