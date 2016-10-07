package smartthings.dw.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.primitives.Ints;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jackson.Jackson;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Response;
import smartthings.dw.logging.LoggingContext;

import javax.inject.Inject;

public class SpringSecurityAuthenticator implements Authenticator<String, OAuthToken> {
	private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
	private static final Escaper URL_ESCAPER = UrlEscapers.urlFormParameterEscaper();

	private final AuthConfiguration config;
	private final Realm realm;
	private final AsyncHttpClient client;
	private final int timeout;

	public SpringSecurityAuthenticator(AuthConfiguration config) {
		this(config, new DefaultAsyncHttpClient());
	}

	@Inject
	public SpringSecurityAuthenticator(AuthConfiguration config, AsyncHttpClient client) {
		this.config = config;
		this.client = client;
		timeout = Ints.checkedCast(config.getRequestTimeout().toMilliseconds());
		realm = new Realm.Builder(config.getUser(), config.getPassword())
			.setUsePreemptiveAuth(true)
			.setScheme(Realm.AuthScheme.BASIC)
			.build();
	}

	@Override
	public Optional<OAuthToken> authenticate(String token) throws AuthenticationException {
		try {
			Response resp = client.prepareGet(authUrl(token))
				.setRequestTimeout(timeout)
				.setRealm(realm)
				.addHeader("Accept", "application/json")
				.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.getLoggingId())
				.execute()
				.get();

			int code = resp.getStatusCode();
			if (code == 200) {
				AuthResponse oauth = MAPPER.readValue(resp.getResponseBodyAsStream(), AuthResponse.class);
				return Optional.fromNullable(buildToken(oauth));
			} else if (code >= 400 && code < 500) {
				return Optional.absent();
			} else {
				throw new AuthenticationException(String.format("Invalid status code found %d", resp.getStatusCode()));
			}
		} catch (Exception e) {
			throw new AuthenticationException("Exception when trying to validate authentication", e);
		}
	}

	private OAuthToken buildToken(AuthResponse resp) {
		if (resp.getClientId() != null && !resp.getClientId().isEmpty()) {
			Optional<User> user = Optional.absent();
			if (resp.getUserName() != null && !resp.getUserName().isEmpty()) {
				// User will be absent in the case of client only tokens
				user = Optional.of(
						new User(resp.getUuid(),
								resp.getUserName(),
								resp.getEmail(),
								resp.getFullName(),
								resp.getAuthorities()));
			}
			return new OAuthToken(user, resp.getScopes(), resp.getClientId());
		} else {
			return null;
		}
	}

	private String authUrl(String token) {
		return String.format("%s/oauth/check_token?token=%s", config.getHost(), URL_ESCAPER.escape(token));
	}
}
