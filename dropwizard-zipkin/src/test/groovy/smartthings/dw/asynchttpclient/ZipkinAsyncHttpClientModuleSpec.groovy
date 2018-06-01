package smartthings.dw.asynchttpclient

import brave.Tracing
import brave.http.HttpTracing
import com.google.inject.Guice
import com.google.inject.Injector
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import smartthings.brave.asynchttpclient.ClientTracing
import smartthings.dw.guice.AbstractDwModule
import spock.lang.Specification


class ZipkinAsyncHttpClientModuleSpec extends Specification {

    HttpTracing tracing

    private static class DepModule extends AbstractDwModule {
        private final HttpTracing tracing
        DepModule(HttpTracing tracing) {
            this.tracing = tracing
        }
        @Override
        protected void configure() {
            bind(HttpTracing).toInstance(tracing)
        }
    }

    def setup() {
        tracing = HttpTracing.create(Tracing.newBuilder().build())
    }

    def 'bind Endpoint, add tracing request filter'() {
        given:
        Injector injector = Guice.createInjector(new DepModule(tracing), new ZipkinAsyncHttpClientModule())

        when:
        DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

        then:
        client.config.requestFilters.find { it instanceof ClientTracing.TracingRequestFilter }
        client.config.responseFilters.find { it instanceof ClientTracing.TracingResponseFilter }
        client.config.ioExceptionFilters.find { it instanceof ClientTracing.TracingIOExceptionFilter }
    }
}
