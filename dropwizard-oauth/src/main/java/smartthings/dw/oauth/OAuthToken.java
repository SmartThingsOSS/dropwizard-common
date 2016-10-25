package smartthings.dw.oauth;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import smartthings.dw.oauth.scope.ScopeSupport;

import java.security.Principal;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

public class OAuthToken implements Principal, ScopeSupport {

	private final Optional<User> user;
	private final Set<String> scopes;
	private final String clientId;
	private final String authToken;

	public OAuthToken(Optional<User> user, Collection<String> scopes, String clientId, String authToken) {
		this.user = user;
		this.scopes = ImmutableSet.copyOf(scopes);
		this.clientId = clientId;
        this.authToken = authToken;
	}

	@Override
	public String getName() {
		if (user.isPresent()) {
			return user.get().getUserName();
		} else {
			return null;
		}
	}

	public Optional<User> getUser() {
		return user;
	}

	@Override
	public Set<String> getScopes() {
		return scopes;
	}

	public String getClientId() {
		return clientId;
	}

	public String getValue() {
        return authToken;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		OAuthToken that = (OAuthToken) o;
		return Objects.equals(user, that.user) &&
			Objects.equals(scopes, that.scopes) &&
			Objects.equals(clientId, that.clientId) &&
            Objects.equals(authToken, that.authToken);
	}

	@Override
	public int hashCode() {
		return Objects.hash(user, scopes, clientId, authToken);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("user", user)
			.add("scopes", scopes)
			.add("clientId", clientId)
			.toString();
	}
}
