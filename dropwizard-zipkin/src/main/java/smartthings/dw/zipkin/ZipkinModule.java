package smartthings.dw.zipkin;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.Sampler;
import com.google.common.net.InetAddresses;
import com.google.inject.TypeLiteral;
import smartthings.dw.guice.AbstractDwModule;
import zipkin.Span;
import zipkin.reporter.Reporter;

public class ZipkinModule extends AbstractDwModule {

    private final ZipkinConfiguration config;

    public ZipkinModule(ZipkinConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        Reporter<Span> reporter = config.getReporter().build();
        Brave brave = new Brave.Builder(
            toInt(config.getServiceHost()),
            config.getServicePort(),
            config.getServiceName()
        )
            .reporter(reporter)
            .traceSampler(Sampler.create(config.getSampleRate()))
            .traceId128Bit(config.isTraceId128Bit())
            .build();

        bind(new TypeLiteral<Reporter<Span>>() {})
            .toInstance(reporter);
        bind(Brave.class).toInstance(brave);
    }

    private static int toInt(final String ip) {
        return InetAddresses.coerceToInteger(InetAddresses.forString(ip));
    }
}
