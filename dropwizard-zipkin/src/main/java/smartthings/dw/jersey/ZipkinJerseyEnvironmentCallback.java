package smartthings.dw.jersey;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.jaxrs2.BraveContainerRequestFilter;
import com.github.kristofa.brave.jaxrs2.BraveContainerResponseFilter;
import com.google.inject.Inject;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.EnvironmentCallback;

public class ZipkinJerseyEnvironmentCallback implements EnvironmentCallback {
    private final static Logger LOG = LoggerFactory.getLogger(ZipkinJerseyEnvironmentCallback.class);

    private final Brave brave;

    @Inject
    public ZipkinJerseyEnvironmentCallback(Brave brave) {
        this.brave = brave;
    }

    @Override
    public void postSetup(Environment environment) {
        // Register the request filter for incoming server requests
        environment
            .jersey()
            .register(BraveContainerRequestFilter.builder(brave)
                .spanNameProvider(new DefaultSpanNameProvider())
                .build());

        // Register the response filter for outgoing server requests
        environment
            .jersey()
            .register(BraveContainerResponseFilter.builder(brave).build());
    }
}
