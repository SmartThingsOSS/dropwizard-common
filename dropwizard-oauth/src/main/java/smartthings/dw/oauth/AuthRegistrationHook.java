package smartthings.dw.oauth;

import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.EnvironmentCallback;
import smartthings.dw.oauth.scope.ExtendedAuthDynamicFeature;
import smartthings.dw.oauth.scope.ScopesAllowedDynamicFeature;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthRegistrationHook implements EnvironmentCallback {
	private final static Logger LOG = LoggerFactory.getLogger(AuthRegistrationHook.class);

	private final ExtendedAuthDynamicFeature authDynamicFeature;
	private final AuthConfiguration configuration;

	@Inject
	public AuthRegistrationHook(ExtendedAuthDynamicFeature authDynamicFeature, AuthConfiguration configuration) {
		this.configuration = configuration;
		this.authDynamicFeature = authDynamicFeature;
	}

	@Override
	public void postSetup(Environment environment) {
		if (configuration.getEnabled()) {
			LOG.info("Registering authentication filters");
			environment.jersey().register(authDynamicFeature);
			environment.jersey().register(RolesAllowedDynamicFeature.class);
			environment.jersey().register(ScopesAllowedDynamicFeature.class);
			environment.jersey().register(new AuthValueFactoryProvider.Binder<>(OAuthToken.class));
		} else {
			LOG.warn("Authentication is currently disabled. All requests are unauthenticated.");
		}
	}
}
