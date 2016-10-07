package smartthings.dw.oauth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import org.asynchttpclient.AsyncHttpClient;
import smartthings.dw.guice.AbstractDwModule;
import smartthings.dw.oauth.scope.ExtendedAuthDynamicFeature;

import java.util.concurrent.TimeUnit;

public class AuthModule extends AbstractDwModule {

	@Override
	protected void configure() {
		bind(AuthRegistrationHook.class).in(Scopes.SINGLETON);
		registerEnvironmentCallback(AuthRegistrationHook.class);
	}

	@Provides
	@Singleton
	ExtendedAuthDynamicFeature authDynamicFeature(
			AuthConfiguration config, MetricRegistry registry, AsyncHttpClient client) {
		Authenticator<String, OAuthToken> authenticator = new SpringSecurityAuthenticator(config, client);

		long cacheMillis = config.getCacheTTL().toMilliseconds();
		if (cacheMillis > 0) {
			CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
					.maximumSize(100)
					.expireAfterWrite(cacheMillis, TimeUnit.MILLISECONDS);
			authenticator = new CachingAuthenticator<>(
					registry,
					authenticator,
					cacheBuilder
			);
		}
		return new ExtendedAuthDynamicFeature(
				new OAuthCredentialAuthFilter.Builder<OAuthToken>()
						.setAuthenticator(authenticator)
						.setAuthorizer(new TokenAuthorizer())
						.setPrefix("Bearer")
						.buildAuthFilter());
	}
}
