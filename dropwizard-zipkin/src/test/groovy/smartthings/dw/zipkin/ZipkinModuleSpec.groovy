package smartthings.dw.zipkin

import com.github.kristofa.brave.Brave
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.TypeLiteral
import com.twitter.zipkin.gen.Endpoint
import spock.lang.Specification
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
        Brave brave = injector.getInstance(Brave)

        then:
        brave != null

        when:
        Reporter<Span> reporter = injector.getInstance(Key.get(new TypeLiteral<Reporter<Span>>() {}))

        then:
        reporter == Reporter.NOOP

        when:
        Endpoint ep = injector.getInstance(Endpoint)

        then:
        ep.service_name == serviceName
        ep.port.toInteger() == servicePort
        ep.ipv4 == (127 << 24 | 1)
    }
}
