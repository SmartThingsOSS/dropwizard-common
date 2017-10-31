package smartthings.dw.asynchttpclient

import brave.Tracing
import brave.http.HttpTracing
import brave.propagation.CurrentTraceContext
import brave.propagation.Propagation
import com.google.inject.Guice
import com.google.inject.Injector
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import smartthings.dw.guice.AbstractDwModule
import spock.lang.Specification

class ZipkinAsyncHttpClientModuleSpec extends Specification {

    HttpTracing httpTracing
    Tracing tracing
    CurrentTraceContext currentTraceContext
    Propagation<String> propagation

    private static class DepModule extends AbstractDwModule {
        private final HttpTracing httpTracing
        DepModule(HttpTracing httpTracing) {
            this.httpTracing = httpTracing
        }
        @Override
        protected void configure() {
            bind(HttpTracing).toInstance(httpTracing)
        }
    }

    def setup() {
        httpTracing = Mock(HttpTracing)
        tracing = Mock(Tracing)
        currentTraceContext = Mock(CurrentTraceContext)
        propagation = Mock(Propagation)

        httpTracing.tracing() >> tracing
        httpTracing.serverName() >> "test-server"
        tracing.currentTraceContext() >> currentTraceContext
        tracing.propagation() >> propagation

    }

    def 'bind Endpoint, add tracing request filter'() {
        given:
        def overrides = [
            connectTimeout: 100,
            acceptAnyCertificate: true,
            readTimeout: 1000
        ]
        Injector injector = Guice.createInjector(new DepModule(httpTracing),
            new ZipkinAsyncHttpClientModule(new AsyncHttpClientConfig(properties: overrides)))

        when:
        DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

        then:
        overrides.each { k, v ->
            assert client.config[k] == v
        }
        client.config.requestFilters.size() == 2
    }
}
