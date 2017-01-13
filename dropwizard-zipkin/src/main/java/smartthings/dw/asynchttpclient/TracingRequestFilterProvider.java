package smartthings.dw.asynchttpclient;

import com.github.kristofa.brave.Brave;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.twitter.zipkin.gen.Endpoint;
import org.asynchttpclient.filter.RequestFilter;
import smartthings.brave.asynchttpclient.NamedSpanNameProvider;
import smartthings.brave.asynchttpclient.TracingRequestFilter;

import javax.inject.Provider;

// TODO: Avoid starting a new client traces if there isn't an active server trace in progress?
public class TracingRequestFilterProvider implements Provider<RequestFilter> {

    private final Brave brave;
    private final Endpoint endpoint;

    @Inject
    public TracingRequestFilterProvider(Brave brave, Endpoint endpoint) {
        this.brave = brave;
        this.endpoint = endpoint;
    }

    @Singleton
    @Override
    public RequestFilter get() {
        return new TracingRequestFilter(
            brave.clientRequestInterceptor(),
            brave.clientResponseInterceptor(),
            new NamedSpanNameProvider(),
            endpoint,
            brave.clientSpanThreadBinder()
        );
    }
}
