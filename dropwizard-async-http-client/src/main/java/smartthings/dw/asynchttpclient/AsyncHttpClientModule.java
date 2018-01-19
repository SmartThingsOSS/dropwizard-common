package smartthings.dw.asynchttpclient;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.multibindings.OptionalBinder;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.config.AsyncHttpClientConfigDefaults;
import org.asynchttpclient.config.AsyncHttpClientConfigHelper;
import org.asynchttpclient.filter.RequestFilter;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsyncHttpClientModule extends AbstractModule {

	private final AsyncHttpClientConfig config;

	public AsyncHttpClientModule() {
		this(new AsyncHttpClientConfig());
	}

	public AsyncHttpClientModule(AsyncHttpClientConfig config) {
		this.config = config;
	}

	@Override
	protected void configure() {
		Multibinder<RequestFilter> requestFilters = Multibinder.newSetBinder(binder(), RequestFilter.class);
		requestFilters.addBinding().to(CorrelationIdFilter.class);

        String prefix = AsyncHttpClientConfigDefaults.ASYNC_CLIENT_CONFIG_ROOT;

        // Set override properties from config so we don't have to use a properties file
        config.getProperties().forEach((name, value) -> System.setProperty(prefix + name, value.toString()));
        AsyncHttpClientConfigHelper.reloadProperties();

        OptionalBinder.newOptionalBinder(binder(), AsyncHttpClient.class)
            .setDefault().toProvider(AsyncHttpClientProvider.class);
	}
}
