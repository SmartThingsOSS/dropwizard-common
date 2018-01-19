package smartthings.dw.asynchttpclient;

import com.google.inject.multibindings.OptionalBinder;
import org.asynchttpclient.AsyncHttpClient;

public class ZipkinAsyncHttpClientModule extends AsyncHttpClientModule {

    public ZipkinAsyncHttpClientModule(AsyncHttpClientConfig config) {
        super(config);
    }

    public ZipkinAsyncHttpClientModule() {
        super();
    }

    @Override
    protected void configure() {
        super.configure();
        OptionalBinder.newOptionalBinder(binder(), AsyncHttpClient.class)
            .setBinding().toProvider(TracingAsyncHttpClientProvider.class);
    }
}
