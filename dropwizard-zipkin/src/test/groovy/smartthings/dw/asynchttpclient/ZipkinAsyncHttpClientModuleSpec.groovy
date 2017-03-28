package smartthings.dw.asynchttpclient

import com.github.kristofa.brave.Brave
import com.google.inject.Guice
import com.google.inject.Injector
import com.twitter.zipkin.gen.Endpoint
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import smartthings.brave.asynchttpclient.TracingRequestFilter
import smartthings.dw.guice.AbstractDwModule
import smartthings.dw.zipkin.ZipkinConfiguration
import spock.lang.Specification

class ZipkinAsyncHttpClientModuleSpec extends Specification {

    ZipkinConfiguration config
    Brave brave
    Endpoint endpoint

    private static class DepModule extends AbstractDwModule {
        private final Brave brave
        private final Endpoint endpoint
        DepModule(Brave brave, Endpoint endpoint) {
            this.brave = brave
            this.endpoint = endpoint
        }
        @Override
        protected void configure() {
            bind(Brave).toInstance(brave)
            bind(Endpoint).toInstance(endpoint)
        }
    }

    def setup() {
        config = Mock(ZipkinConfiguration)
        brave = Mock(Brave)
        endpoint = Mock(Endpoint)
    }

    def 'bind Endpoint, add tracing request filter'() {
        given:
        Injector injector = Guice.createInjector(new DepModule(brave, endpoint), new ZipkinAsyncHttpClientModule())

        when:
        DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

        then:
        client.config.requestFilters.find { it instanceof TracingRequestFilter }
    }
}
