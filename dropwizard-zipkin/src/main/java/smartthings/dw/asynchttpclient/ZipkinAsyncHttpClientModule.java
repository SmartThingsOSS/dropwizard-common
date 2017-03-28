package smartthings.dw.asynchttpclient;

import com.google.inject.multibindings.Multibinder;
import org.asynchttpclient.filter.RequestFilter;

public class ZipkinAsyncHttpClientModule extends AsyncHttpClientModule {

    public ZipkinAsyncHttpClientModule() {
        super();
    }

    public ZipkinAsyncHttpClientModule(AsyncHttpClientConfig ahcConfig) {
        super(ahcConfig);
    }

    @Override
    protected void configure() {
        super.configure();
        Multibinder.newSetBinder(binder(), RequestFilter.class)
            .addBinding()
            .toProvider(TracingRequestFilterProvider.class);
    }
}
