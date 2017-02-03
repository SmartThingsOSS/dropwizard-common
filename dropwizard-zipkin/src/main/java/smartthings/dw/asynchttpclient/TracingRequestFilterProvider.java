package smartthings.dw.asynchttpclient;

import com.github.kristofa.brave.Brave;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.twitter.zipkin.gen.Endpoint;
import org.asynchttpclient.filter.RequestFilter;
import smartthings.brave.asynchttpclient.NamedSpanNameProvider;
import smartthings.brave.asynchttpclient.TracingRequestFilter;

import javax.inject.Provider;

public class TracingRequestFilterProvider implements Provider<RequestFilter> {

    private final Brave brave;
    private final Endpoint endpoint;
    private final TracingRequestFilterConfiguration config;

    @Inject
    public TracingRequestFilterProvider(Brave brave, Endpoint endpoint, TracingRequestFilterConfiguration config) {
        this.brave = brave;
        this.endpoint = endpoint;
        this.config = config;
    }

    @Singleton
    @Override
    public RequestFilter get() {
        return new TracingRequestFilter(
            brave.clientRequestInterceptor(),
            brave.clientResponseInterceptor(),
            new NamedSpanNameProvider(),
            endpoint,
            brave.clientSpanThreadBinder(),
            brave.serverSpanThreadBinder(),
            config.getStartNewTraces()
        );
    }
}
