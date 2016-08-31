package smartthings.dw.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
	private String userName;
	private List<String> scopes;
	private List<String> authorities;
	private String clientId;
	private String uuid;
	private String email;
	private String fullName;

	public AuthResponse() {
	}

	@JsonProperty("user_name")
	public String getUserName() {
		return userName;
	}

	@JsonProperty("user_name")
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@JsonProperty("scope")
	public List<String> getScopes() {
		if (scopes == null) {
			return new ArrayList<>();
		}
		return scopes;
	}

	@JsonProperty("scope")
	public void setScopes(List<String> scope) {
		this.scopes = scope;
	}

	public List<String> getAuthorities() {
		if (authorities == null) {
			return new ArrayList<>();
		}
		return authorities;
	}

	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}

	@JsonProperty("client_id")
	public String getClientId() {
		return clientId;
	}

	@JsonProperty("client_id")
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@JsonProperty("fullName")
	public String getFullName() { return fullName; }

	@JsonProperty("fullName")
	public void setFullName(String fullName) { this.fullName = fullName; }

	@JsonProperty("email")
	public String getEmail() { return email; }

	@JsonProperty("email")
	public void setEmail(String email) { this.email = email; }
}
