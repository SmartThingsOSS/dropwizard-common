package smartthings.dw.oauth;

import io.dropwizard.util.Duration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

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
}
