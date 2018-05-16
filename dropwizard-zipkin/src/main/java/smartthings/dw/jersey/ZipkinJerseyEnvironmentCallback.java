package smartthings.dw.jersey;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.jersey.server.TracingApplicationEventListener;
import com.google.inject.Inject;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.EnvironmentCallback;

public class ZipkinJerseyEnvironmentCallback implements EnvironmentCallback {
    private final static Logger LOG = LoggerFactory.getLogger(ZipkinJerseyEnvironmentCallback.class);

    private final HttpTracing httpTracing;

    @Inject
    public ZipkinJerseyEnvironmentCallback(Tracing tracing) {
        httpTracing = HttpTracing.create(tracing);
    }

    @Override
    public void postSetup(Environment environment) {
        // Register the request filter for incoming server requests
        environment.jersey().register(TracingApplicationEventListener.create(httpTracing));
    }
}
