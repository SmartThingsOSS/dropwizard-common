package smartthings.dw.asynchttpclient;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.filter.RequestFilter;

import javax.inject.Provider;
import java.util.Set;

public class AsyncHttpClientProvider implements Provider<AsyncHttpClient> {

    private final Set<RequestFilter> requestFilters;

    @Inject
    public AsyncHttpClientProvider(Set<RequestFilter> requestFilters) {
        this.requestFilters = requestFilters;
    }

    @Singleton
    @Override
    public AsyncHttpClient get() {

        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();

        requestFilters.forEach(builder::addRequestFilter);

        return new DefaultAsyncHttpClient(builder.build());
    }
}
