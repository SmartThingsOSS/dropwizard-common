package smartthings.dw.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.common.primitives.Ints;
import com.google.inject.Singleton;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.jackson.Jackson;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Response;
import smartthings.dw.logging.LoggingContext;
import smartthings.dw.oauth.exceptions.*;

import javax.inject.Inject;
import java.util.Optional;

@Singleton
public class SpringSecurityAuthenticator implements OAuthAuthenticator {
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
		int code;
		try {
			Response resp = client.preparePost(config.getHost() + "/oauth/check_token")
				.setRequestTimeout(timeout)
				.setRealm(realm)
				.addHeader("Accept", "application/json")
				.addHeader(LoggingContext.CORRELATION_ID_HEADER, LoggingContext.getLoggingId())
				.addFormParam("token", URL_ESCAPER.escape(token))
				.execute()
				.get();

			code = resp.getStatusCode();
			if (code == 200) {
				AuthResponse authResponse = MAPPER.readValue(resp.getResponseBodyAsStream(), AuthResponse.class);
				return Optional.ofNullable(authResponse.toOAuthToken(token));
			}
		} catch (Exception e) {
			throw new AuthenticationException("Exception when trying to validate authentication", e);
		}

		if (code >= 400 && code < 500) {
			return Optional.empty();
		} else if (code >= 500) {
			if (code == 520) {
				throw new UnknownException();
			} else if (code == 521) {
				throw new CircuitBreakerOpenException();
			} else if (code == 522) {
				throw new ConnectionException();
			} else if (code == 524) {
				throw new TimeoutException();
			} else if (code == 525) {
				throw new SSLException();
			}
		}

		throw new AuthenticationException(String.format("Invalid status code found %d", code));
	}
}
