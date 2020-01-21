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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.exceptions.TransparentResponseStatusException;
import smartthings.dw.logging.LoggingContext;

import javax.inject.Inject;
import java.util.Optional;

@Singleton
public class SpringSecurityAuthenticator implements OAuthAuthenticator {
	private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
	private static final Escaper URL_ESCAPER = UrlEscapers.urlFormParameterEscaper();
	private static final Logger LOG = LoggerFactory.getLogger(SpringSecurityAuthenticator.class);

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
        } else if (code >= 520 && code < 530) {
            LOG.warn("Authentication timeout encountered");
            return Optional.empty();
        } else if (config.getTransparentServerStatusCodes().contains(code)) {
            throw new TransparentResponseStatusException(code);
        }

        throw new AuthenticationException(String.format("Invalid status code found %d", code));
	}
}
