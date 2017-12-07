package smartthings.dw.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.inject.Stage
import io.dropwizard.Application
import io.dropwizard.Configuration
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
import static com.github.tomakehurst.wiremock.client.WireMock.*

class AuthModuleSpec extends Specification {

    static final int WIRE_MOCK_PORT = 11224

    @ClassRule
    @Shared
    WireMockRule authMock = new WireMockRule(WireMockConfiguration.wireMockConfig().port(WIRE_MOCK_PORT))

    @ClassRule
    @Shared
    DropwizardAppRule<TestConfiguration> app = new DropwizardAppRule<>(TestApplication.class, new TestConfiguration(
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
        String token = UUID.randomUUID().toString()
        AuthResponse authResponse = new AuthResponse(
            userName: 'elmo',
            scopes: ['superuser'],
            uuid: UUID.randomUUID().toString(),
            clientId: UUID.randomUUID().toString()
        )
        authMock
            .stubFor(post(urlEqualTo("/oauth/check_token"))
            .withRequestBody(containing(token))
            .willReturn(aResponse().withStatus(200).withBody(mapper.writeValueAsBytes(authResponse)))
        )

        when:
        Response response = client.prepareGet("http://localhost:${app.getLocalPort()}/methodProtected")
            .addHeader('Authorization', "Bearer ${token}")
            .execute()
            .get()

        then:
        authMock.verify(postRequestedFor(urlEqualTo('/oauth/check_token')))

        assert response.statusCode == 200
    }

}

class TestApplication extends Application<TestConfiguration> {
    @Override
    void run(TestConfiguration configuration, Environment environment) throws Exception {
        DwGuice.from(Stage.PRODUCTION,
            new TestModule(configuration),
        ).register(environment)
    }
}

class TestModule extends AbstractDwModule {

    TestConfiguration config

    TestModule(TestConfiguration config) {
        this.config = config
    }

    @Override
    protected void configure() {
        bind(AsyncHttpClient).toInstance(new DefaultAsyncHttpClient())
        bind(AuthConfiguration).toInstance(config.auth)
        install(new AuthModule())
        registerResource(TestScopeResource)
    }
}

class TestConfiguration extends Configuration {
    AuthConfiguration auth
}
