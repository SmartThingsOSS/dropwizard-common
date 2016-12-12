package smartthings.dw.oauth.scope

import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.Authenticator
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.testing.junit.ResourceTestRule
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory
import org.junit.Rule
import smartthings.dw.oauth.OAuthToken
import smartthings.dw.oauth.User
import smartthings.dw.oauth.TokenAuthorizer
import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ScopesAllowedDynamicFeatureSpec extends Specification {

	Authenticator<String, OAuthToken> authenticator = Mock()

	@Rule
	public ResourceTestRule rule = ResourceTestRule
		.builder()
		.setTestContainerFactory(new GrizzlyWebTestContainerFactory())
		.addProvider(new ExtendedAuthDynamicFeature(new OAuthCredentialAuthFilter.Builder<OAuthToken>()
		.setAuthenticator(authenticator)
		.setAuthorizer(new TokenAuthorizer())
		.setPrefix("Bearer")
		.buildAuthFilter()))
		.addProvider(ScopesAllowedDynamicFeature.class)
		.addProvider(RolesAllowedDynamicFeature.class)
		.addProvider(new AuthValueFactoryProvider.Binder<OAuthToken>(OAuthToken.class))
		.addResource(new TestScopeResource())
		.build();

	@Unroll
	def 'scopes only tests - scopes #scopes - path #path - status #status'() {
		given:
		OAuthToken token = new OAuthToken(Optional.empty(), scopes, "", "TOKEN", [:])

		when:
		Response response = rule.getJerseyTest().target(path)
			.request(MediaType.APPLICATION_JSON_TYPE)
			.header("Authorization", "Bearer TOKEN")
			.get();

		then:
		1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
		0 * _

		response.status == status

		where:
		scopes        | path               || status
		['admin']     | "/classProtected"  || 200
		['superuser'] | "/classProtected"  || 403
		['superuser'] | "/methodProtected" || 200
		['admin']     | "/methodProtected" || 403
	}

	@Unroll
	def 'role tests - scopes #scopes - path #path - status #status'() {
		given:
		User user = new User(null, "charliek", "", "", roles)
		OAuthToken token = new OAuthToken(Optional.of(user), scopes, "", "TOKEN", [:])

		when:
		Response response = rule.getJerseyTest().target(path)
				.request(MediaType.APPLICATION_JSON_TYPE)
				.header("Authorization", "Bearer TOKEN")
				.get();

		then:
		1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
		0 * _

		response.status == status

		where:
		roles         | scopes        | path                             || status
		['DEVELOPER'] | ['admin']     | "/methodProtectedWithRole"       || 200
		['DEVELOPER'] | ['superuser'] | "/methodProtectedWithRole"       || 403
		['DEVELOPER'] | ['superuser'] | "/methodProtectedWithRoleScope"  || 200
		['DEVELOPER'] | ['admin']     | "/methodProtectedWithRoleScope"  || 403
	}

}
