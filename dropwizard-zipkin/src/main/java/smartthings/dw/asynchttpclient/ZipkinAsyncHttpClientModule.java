package smartthings.dw.asynchttpclient;

import brave.Tracing;
import brave.http.HttpTracing;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.filter.IOExceptionFilter;
import org.asynchttpclient.filter.RequestFilter;
import org.asynchttpclient.filter.ResponseFilter;
import smartthings.brave.asynchttpclient.ClientTracing;

import java.util.Set;


public class ZipkinAsyncHttpClientModule extends AsyncHttpClientModule {

    @Inject
    private HttpTracing httpTracing;

    public ZipkinAsyncHttpClientModule() {
        super();
    }

    public ZipkinAsyncHttpClientModule(AsyncHttpClientConfig ahcConfig) {
        super(ahcConfig);
    }

    @Override
    protected void configure() {
        super.configure();
        requestInjection(this);
    }

    @Override
    void customizeBuilder(DefaultAsyncHttpClientConfig.Builder builder) {
        super.customizeBuilder(builder);
        ClientTracing.instrument(builder, httpTracing);
    }
}
