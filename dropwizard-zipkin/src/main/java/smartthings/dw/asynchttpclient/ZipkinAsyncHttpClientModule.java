package smartthings.dw.asynchttpclient;

import com.google.inject.multibindings.Multibinder;
import com.twitter.zipkin.gen.Endpoint;
import org.asynchttpclient.filter.RequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.zipkin.ZipkinConfiguration;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;

public class ZipkinAsyncHttpClientModule extends AsyncHttpClientModule {

    private final static Logger LOG = LoggerFactory.getLogger(ZipkinAsyncHttpClientModule.class);

    private final ZipkinConfiguration zipkinConfig;
    private final TracingRequestFilterConfiguration tracingfilterConfig;

    public ZipkinAsyncHttpClientModule(ZipkinConfiguration zipkinConfig) {
        super();
        this.zipkinConfig = zipkinConfig;
        this.tracingfilterConfig = new TracingRequestFilterConfiguration();
    }

    public ZipkinAsyncHttpClientModule(ZipkinConfiguration zipkinConfig,
                                       AsyncHttpClientConfig ahcConfig,
                                       TracingRequestFilterConfiguration tracingfilterConfig
                                       ) {
        super(ahcConfig);
        this.zipkinConfig = zipkinConfig;
        this.tracingfilterConfig = tracingfilterConfig;
    }

    @Override
    protected void configure() {
        super.configure();
        bind(Endpoint.class).toInstance(endpoint());
        Multibinder.newSetBinder(binder(), RequestFilter.class)
            .addBinding()
            .toProvider(TracingRequestFilterProvider.class);
    }

    Endpoint endpoint() {
        int ipv4 = 127 << 24 | 1;
        try {
            ipv4 = hostAddress()
                .map(addr -> (new BigInteger(addr.getAddress())).intValue())
                .orElse(ipv4);

        } catch (SocketException e) {
            LOG.error("Failed to get host IPv4 address", e);
        }
        return Endpoint.builder()
            .serviceName(zipkinConfig.getServiceName())
            .ipv4(ipv4)
            .port(zipkinConfig.getServicePort())
            .build();
    }

    public static Optional<InetAddress> hostAddress() throws SocketException {
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
