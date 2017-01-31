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

    private static class DepModule extends AbstractDwModule {
        private final Brave brave
        DepModule(Brave brave) {
            this.brave = brave
        }
        @Override
        protected void configure() {
            bind(Brave).toInstance(brave)
        }
    }

    def setup() {
        config = Mock(ZipkinConfiguration)
        brave = Mock(Brave)
    }

    def 'bind Endpoint, add tracing request filter'() {
        given:
        def serviceName = "dw-zipkin-test"
        def servicePort = 1337
        config.getServiceName() >> serviceName
        config.getServicePort() >> servicePort
        Injector injector = Guice.createInjector(new DepModule(brave), new ZipkinAsyncHttpClientModule(config))

        when:
        Endpoint ep = injector.getInstance(Endpoint)

        then:
        ep.service_name == serviceName
        servicePort == ep.port.toInteger()

        when:
        DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

        then:
        client.config.requestFilters.find { it instanceof TracingRequestFilter }
    }
}
