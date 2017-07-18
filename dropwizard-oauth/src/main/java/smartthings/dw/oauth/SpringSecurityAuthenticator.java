package smartthings.dw.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.primitives.Ints;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jackson.Jackson;
import org.asynchttpclient.*;
import smartthings.dw.logging.LoggingContext;

import javax.inject.Inject;
import java.util.Optional;

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
			BoundRequestBuilder reqBuilder = client.preparePost(config.getHost() + "/oauth/check_token")
					.setRequestTimeout(timeout)
					.setRealm(realm)
					.addHeader("Accept", "application/json")
					.addFormParam("token", URL_ESCAPER.escape(token));
			if (LoggingContext.getLoggingId() != null) {
				reqBuilder.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.getLoggingId());
			}
			Response resp = reqBuilder.execute().get();

			int code = resp.getStatusCode();
			if (code == 200) {
				AuthResponse oauth = MAPPER.readValue(resp.getResponseBodyAsStream(), AuthResponse.class);
				return Optional.ofNullable(buildToken(oauth, token));
			} else if (code >= 400 && code < 500) {
				return Optional.empty();
			} else {
				throw new AuthenticationException(String.format("Invalid status code found %d", resp.getStatusCode()));
			}
		} catch (Exception e) {
			throw new AuthenticationException("Exception when trying to validate authentication", e);
		}
	}

	private OAuthToken buildToken(AuthResponse resp, String token) {
		if (resp.getClientId() != null && !resp.getClientId().isEmpty()) {
			Optional<User> user = Optional.empty();
			if (resp.getUserName() != null && !resp.getUserName().isEmpty()) {
				// User will be absent in the case of client only tokens
				user = Optional.of(
						new User(resp.getUuid(),
								resp.getUserName(),
								resp.getEmail(),
								resp.getFullName(),
								resp.getAuthorities()));
			}
			return new OAuthToken(user, resp.getScopes(), resp.getClientId(), token, resp.getAdditionalFields());
		} else {
			return null;
		}
	}
}
