package smartthings.dw.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.inject.Stage
import com.google.inject.multibindings.OptionalBinder
import io.dropwizard.Application
import io.dropwizard.Configuration
import io.dropwizard.auth.AuthenticationException
import io.dropwizard.auth.Authenticator
import io.dropwizard.setup.Environment
import io.dropwizard.testing.junit.DropwizardAppRule
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.Response
import org.junit.ClassRule
import smartthings.dw.guice.AbstractDwModule
import smartthings.dw.guice.DwGuice
import smartthings.dw.oauth.scope.TestScopeResource
import spock.lang.Shared
import spock.lang.Specification

class AuthModuleOverrideSpec extends Specification {

    static final int WIRE_MOCK_PORT = 11224

    @ClassRule
    @Shared
    WireMockRule authMock = new WireMockRule(WireMockConfiguration.wireMockConfig().port(WIRE_MOCK_PORT))

    @ClassRule
    @Shared
    DropwizardAppRule<TestOverrideConfiguration> app = new DropwizardAppRule<>(TestOverrideApplication.class, new TestOverrideConfiguration(
        auth: new AuthConfiguration(
            host: "http://localhost:${WIRE_MOCK_PORT}",
            user: 'user',
            password: 'password'
        )
    ))

    @Shared
    AsyncHttpClient client = new DefaultAsyncHttpClient()

    @Shared
    ObjectMapper mapper = new ObjectMapper()

    void setup() {
        authMock.resetAll()
    }

    void 'it should validate request authentication'() {
        given:
        String token = 'good token'

        when:
        Response response = client.prepareGet("http://localhost:${app.getLocalPort()}/methodProtected")
            .addHeader('Authorization', "Bearer ${token}")
            .execute()
            .get()

        then:
        assert response.statusCode == 200
    }

    void 'it should not validate a request with bad authentication'() {
        given:
        String token = 'bad token'

        when:
        Response response = client.prepareGet("http://localhost:${app.getLocalPort()}/methodProtected")
            .addHeader('Authorization', "Bearer ${token}")
            .execute()
            .get()

        then:
        assert response.statusCode == 401
    }

}

class TestOverrideApplication extends Application<TestOverrideConfiguration> {
    @Override
    void run(TestOverrideConfiguration configuration, Environment environment) throws Exception {
        DwGuice.from(Stage.PRODUCTION,
            new TestOverrideModule(configuration),
        ).register(environment)
    }
}

class TestOverrideModule extends AbstractDwModule {

    TestOverrideConfiguration config

    TestOverrideModule(TestOverrideConfiguration config) {
        this.config = config
    }

    @Override
    protected void configure() {
        bind(AsyncHttpClient).toInstance(new DefaultAsyncHttpClient())
        bind(AuthConfiguration).toInstance(config.auth)
        OptionalBinder.newOptionalBinder(binder(), OAuthAuthenticator.class)
            .setBinding().toInstance(buildAuthenticator())

        install(new AuthModule())
        registerResource(TestScopeResource)
    }

    static OrderedPredicateAuthenticator buildAuthenticator() {
        return OrderedPredicateAuthenticator.newBuilder()
            .addPredicateAuthenticatorTuple(
            { token ->
                if (token.contains('good')) {
                    return true
                }
                return false
            },
            new Authenticator<String, OAuthToken>() {
                @Override
                Optional<OAuthToken> authenticate(String token) throws AuthenticationException {
                    return Optional.of(
                        new OAuthToken(
                            Optional.of(
                                new User(UUID.randomUUID().toString(), 'elmo', 'elmo@smartthings.com', 'elmo', ['ROLE_USER'])
                            ),
                            ['superuser'],
                            UUID.randomUUID().toString(),
                            token,
                            [:]
                        )
                    )
                }
            }
        ).build()
    }
}

class TestOverrideConfiguration extends Configuration {
    AuthConfiguration auth
}
