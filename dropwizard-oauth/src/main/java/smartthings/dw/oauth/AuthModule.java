package smartthings.dw.oauth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.multibindings.OptionalBinder;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.CachingAuthenticator;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
import smartthings.dw.guice.AbstractDwModule;
import smartthings.dw.oauth.scope.ExtendedAuthDynamicFeature;
import java.util.concurrent.TimeUnit;

public class AuthModule extends AbstractDwModule {

    @Override
    protected void configure() {
        bind(AuthRegistrationHook.class).in(Scopes.SINGLETON);
        registerEnvironmentCallback(AuthRegistrationHook.class);
        OptionalBinder.newOptionalBinder(binder(), OAuthAuthenticator.class)
            .setDefault().to(SpringSecurityAuthenticator.class);
    }

    @Provides
    @Singleton
    ExtendedAuthDynamicFeature authDynamicFeature(
        AuthConfiguration config,
        MetricRegistry registry,
        OAuthAuthenticator auth
    ) {
        long cacheMillis = config.getCacheTTL().toMilliseconds();
        Authenticator<String, OAuthToken> authenticator = auth;
        if (cacheMillis > 0) {
            CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(config.getCacheSize())
                .expireAfterWrite(cacheMillis, TimeUnit.MILLISECONDS);
            authenticator = new CachingAuthenticator<>(
                registry,
                auth,
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
