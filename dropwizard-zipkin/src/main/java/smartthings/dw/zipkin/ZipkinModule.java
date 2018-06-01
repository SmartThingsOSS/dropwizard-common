package smartthings.dw.zipkin;

import brave.CurrentSpanCustomizer;
import brave.Tracing;
import brave.http.HttpRuleSampler;
import brave.http.HttpTracing;
import brave.sampler.BoundarySampler;
import com.google.common.net.InetAddresses;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.AbstractDwModule;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.net.*;
import java.util.Enumeration;
import java.util.Optional;

public class ZipkinModule extends AbstractDwModule {

    private final static Logger LOG = LoggerFactory.getLogger(ZipkinModule.class);

    private final ZipkinConfiguration config;
    private final HttpRuleSampler serverSampler;
    private final HttpRuleSampler clientSampler;

    public ZipkinModule(ZipkinConfiguration config) {
        this(config, null, null);
    }

    public ZipkinModule(ZipkinConfiguration config, HttpRuleSampler serverSampler, HttpRuleSampler clientSampler) {
        this.config = config;
        this.serverSampler = serverSampler;
        this.clientSampler = clientSampler;
    }

    @Override
    protected void configure() {
        Endpoint endpoint = getHostEndpoint();
        Reporter<Span> reporter = config.getReporter().build();

        Tracing tracing = Tracing.newBuilder()
            .endpoint(endpoint)
            .localServiceName(config.getServiceName())
            .spanReporter(reporter)
            .sampler(BoundarySampler.create(config.getSampleRate()))
            .traceId128Bit(config.isTraceId128Bit())
            .build();

        bind(new TypeLiteral<Reporter<Span>>() {
        }).toInstance(reporter);

        bind(Tracing.class).toInstance(tracing);
        bind(Endpoint.class).toInstance(endpoint);
    }

    @Provides
    @Singleton
    HttpTracing providesHttpTracing(Tracing tracing) {
         HttpTracing.Builder builder = HttpTracing.newBuilder(tracing);

         if (clientSampler != null) {
             builder.clientSampler(clientSampler);
         }

         if (serverSampler != null) {
             builder.serverSampler(serverSampler);
         }

         return builder.build();
    }

    @Provides
    @Singleton
    CurrentSpanCustomizer providesCurrentSpanCustomizer(Tracing tracing) {
        return CurrentSpanCustomizer.create(tracing);
    }

    private static int toInt(final String ip) {
        return InetAddresses.coerceToInteger(InetAddresses.forString(ip));
    }

    Endpoint getHostEndpoint() {
        InetAddress ipv4 = null;

        try {
            ipv4 = InetAddress.getByName("127.0.0.1");

            if (config.getServiceHost() != null) {
                ipv4 = InetAddress.getByName(config.getServiceHost());
            } else {
                ipv4 = hostAddress().orElse(ipv4);
            }
        } catch (Exception e) {
            LOG.error("Failed to get host IPv4 address", e);
        }
        return Endpoint.newBuilder()
            .serviceName(config.getServiceName())
            .ip(ipv4)
            .port(config.getServicePort())
            .build();
    }

    private static Optional<InetAddress> hostAddress() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            Enumeration<InetAddress> addresses = interfaces.nextElement().getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address instanceof Inet4Address && address.isSiteLocalAddress()) {
                    return Optional.of(address);
                }
            }
        }
        return Optional.empty();
    }
}
