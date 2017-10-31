package smartthings.dw.zipkin

import brave.Tracing
import brave.http.HttpTracing
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import spock.lang.Specification
import zipkin.Endpoint
import zipkin.Span
import zipkin.reporter.Reporter

class ZipkinModuleSpec extends Specification {

    ZipkinConfiguration config


    def setup() {
        config = Mock(ZipkinConfiguration)
    }

    def 'bind Brave, Span Reporter and Endpoint'() {
        given:
        def serviceName = "dw-zipkin-test"
        def servicePort = 1337
        config.getServiceHost() >> "127.0.0.1"
        config.getServiceName() >> serviceName
        config.getServicePort() >> servicePort
        config.reporter >> new EmptySpanReporterFactory()
        Injector injector = Guice.createInjector(new ZipkinModule(config))

        when:
        Tracing tracing = injector.getInstance(Tracing)
        HttpTracing httpTracing = injector.getInstance(HttpTracing)

        then:
        tracing != null
        httpTracing != null

        when:
        Reporter<Span> reporter = injector.getInstance(Key.get(new TypeLiteral<Reporter<Span>>() {}))

        then:
        reporter == Reporter.NOOP

        when:
        Endpoint ep = injector.getInstance(Endpoint)

        then:
        ep.serviceName == serviceName
        ep.port.toInteger() == servicePort
        ep.ipv4 == (127 << 24 | 1)
    }
}
