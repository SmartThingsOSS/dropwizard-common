package smartthings.dw.asynchttpclient;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.config.AsyncHttpClientConfigDefaults;
import org.asynchttpclient.config.AsyncHttpClientConfigHelper;
import org.asynchttpclient.filter.IOExceptionFilter;
import org.asynchttpclient.filter.RequestFilter;
import org.asynchttpclient.filter.ResponseFilter;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AsyncHttpClientModule extends AbstractModule {

	protected final static Map<String, String> DEFAULT_OVERRIDES = Collections.unmodifiableMap(
			Stream.of(
					new SimpleImmutableEntry<>("requestTimeout", "5000"),
					new SimpleImmutableEntry<>("readTimeout", "5000")
			).collect(
					Collectors.toMap(e -> e.getKey(), (e) -> e.getValue())
			));

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
        Multibinder<ResponseFilter> responseFilters = Multibinder.newSetBinder(binder(), ResponseFilter.class);
		Multibinder<IOExceptionFilter> ioExceptionFilters = Multibinder.newSetBinder(binder(), IOExceptionFilter.class);
	}

	@Provides
	@Singleton
	AsyncHttpClient asyncHttpClient(
	    Set<RequestFilter> requestFilters,
        Set<ResponseFilter> responseFilters,
        Set<IOExceptionFilter> ioExceptionFilters
    ) {
		return new DefaultAsyncHttpClient(getBuilder(requestFilters, responseFilters, ioExceptionFilters).build());
	}

	protected DefaultAsyncHttpClientConfig.Builder getBuilder(
        Set<RequestFilter> requestFilters,
        Set<ResponseFilter> responseFilters,
        Set<IOExceptionFilter> ioExceptionFilters
    ) {
        String prefix = AsyncHttpClientConfigDefaults.ASYNC_CLIENT_CONFIG_ROOT;
        // Set default overrides
        DEFAULT_OVERRIDES.forEach((name, value) -> System.setProperty(prefix + name, value.toString()));

        // Set override properties from config so we don't have to use a properties file
        config.getProperties().forEach((name, value) -> System.setProperty(prefix + name, value.toString()));
        AsyncHttpClientConfigHelper.reloadProperties();

        DefaultAsyncHttpClientConfig.Builder builder = new DefaultAsyncHttpClientConfig.Builder();

        builder.addRequestFilter(new CorrelationIdFilter());

        requestFilters.forEach(builder::addRequestFilter);
        responseFilters.forEach(builder::addResponseFilter);
        ioExceptionFilters.forEach(builder::addIOExceptionFilter);

        customizeBuilder(builder);

        return builder;
    }

    void customizeBuilder(DefaultAsyncHttpClientConfig.Builder builder) {
    }
}
