package smartthings.dw.jersey;

import brave.http.HttpTracing;
import brave.jaxrs2.TracingFeature;
import com.google.inject.Inject;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.EnvironmentCallback;

import javax.ws.rs.core.Feature;

public class ZipkinJerseyEnvironmentCallback implements EnvironmentCallback {
    private final static Logger LOG = LoggerFactory.getLogger(ZipkinJerseyEnvironmentCallback.class);

    private final Feature tracingFeature;

    @Inject
    public ZipkinJerseyEnvironmentCallback(HttpTracing httpTracing) {
        tracingFeature = TracingFeature.create(httpTracing);
    }

    @Override
    public void postSetup(Environment environment) {
        // Register the request filter for incoming server requests
        environment
            .jersey()
            .register(tracingFeature);
    }
}
