package smartthings.dw.zipkin;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.sampler.BoundarySampler;
import com.google.common.net.InetAddresses;
import com.google.inject.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.AbstractDwModule;
import zipkin.Endpoint;
import zipkin.Span;
import zipkin.reporter.Reporter;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;

public class ZipkinModule extends AbstractDwModule {

    private final static Logger LOG = LoggerFactory.getLogger(ZipkinModule.class);

    private final ZipkinConfiguration config;

    public ZipkinModule(ZipkinConfiguration config) {
        this.config = config;
    }

    @Override
    protected void configure() {
        Endpoint endpoint = getHostEndpoint();
        Reporter<Span> reporter = config.getReporter().build();
        Tracing tracing = Tracing.newBuilder()
            .localEndpoint(endpoint)
            .localServiceName(config.getServiceName())
            .reporter(reporter)
            .sampler(BoundarySampler.create(config.getSampleRate()))
            .traceId128Bit(config.isTraceId128Bit())
            .build();

        bind(new TypeLiteral<Reporter<Span>>() {
        }).toInstance(reporter);
        bind(Tracing.class).toInstance(tracing);
        bind(HttpTracing.class).toInstance(HttpTracing.create(tracing));
        bind(Endpoint.class).toInstance(endpoint);
    }

    private static int toInt(final String ip) {
        return InetAddresses.coerceToInteger(InetAddresses.forString(ip));
    }

    private Endpoint getHostEndpoint() {
        int ipv4 = 127 << 24 | 1;
        try {
            if (config.getServiceHost() != null) {
                ipv4 = toInt(config.getServiceHost());
            } else {
                ipv4 = hostAddress()
                    .map(addr -> (new BigInteger(addr.getAddress())).intValue())
                    .orElse(ipv4);
            }
        } catch (SocketException e) {
            LOG.error("Failed to get host IPv4 address", e);
        }
        return Endpoint.builder()
            .serviceName(config.getServiceName())
            .ipv4(ipv4)
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
