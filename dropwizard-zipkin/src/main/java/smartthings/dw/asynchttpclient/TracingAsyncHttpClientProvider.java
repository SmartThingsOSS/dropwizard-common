package smartthings.dw.asynchttpclient;

import brave.http.HttpTracing;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.filter.RequestFilter;
import smartthings.brave.asynchttpclient.ClientTracing;

import javax.inject.Provider;
import java.util.Set;

public class TracingAsyncHttpClientProvider implements Provider<AsyncHttpClient> {

    private final HttpTracing httpTracing;

    private final Set<RequestFilter> requestFilters;

    @Inject
    public TracingAsyncHttpClientProvider(
        Set<RequestFilter> requestFilters,
        HttpTracing httpTracing
    ) {
            this.httpTracing = httpTracing;
            this.requestFilters = requestFilters;
    }

    @Singleton
    @Override
    public AsyncHttpClient get() {
        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();
        // order of request filter matters; tracing filters should be added last
        requestFilters.forEach(builder::addRequestFilter);
        return new DefaultAsyncHttpClient(ClientTracing.instrument(builder, httpTracing).build());
    }
}
