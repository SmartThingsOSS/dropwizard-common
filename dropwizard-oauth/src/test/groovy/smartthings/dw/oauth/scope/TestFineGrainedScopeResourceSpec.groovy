package smartthings.dw.oauth.scope

import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.auth.Authenticator
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter
import io.dropwizard.testing.junit.ResourceTestRule
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory
import org.junit.Rule
import smartthings.dw.oauth.OAuthToken
import smartthings.dw.oauth.TokenAuthorizer
import smartthings.dw.oauth.User
import spock.lang.Specification

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class TestFineGrainedScopeResourceSpec extends Specification {

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
        .addResource(new TestFineGrainedScopeResource())
        .build()


    def 'should allow standalone FineGrainedScope annotations'() {
        given:
        User user = new User(null, "user", "", "", [""])
        OAuthToken token = new OAuthToken(Optional.of(user), ['r:path:param'], "", "TOKEN", [:])

        when:
        Response response = rule.getJerseyTest().target("/fineGrained/standalone/param")
            .request(MediaType.APPLICATION_JSON_TYPE)
            .header("Authorization", "Bearer TOKEN")
            .get()

        then:
        1 * authenticator.authenticate('TOKEN') >> Optional.of(token)
        0 * _

        and:
        response.status == 200
    }

}
