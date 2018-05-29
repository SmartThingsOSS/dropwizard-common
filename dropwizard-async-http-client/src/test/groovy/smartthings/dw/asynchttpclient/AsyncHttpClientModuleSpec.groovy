package smartthings.dw.asynchttpclient

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.multibindings.Multibinder
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClient
import org.asynchttpclient.filter.RequestFilter

class AsyncHttpClientModuleSpec extends spock.lang.Specification {

	byte[] properties

	def setup() {
		def baos= new ByteArrayOutputStream()
		System.properties.store(baos, '')
		properties = baos.toByteArray()
	}

	def cleanup() {
		System.properties.clear()
		System.properties.load(new ByteArrayInputStream(properties))
	}

	def 'default config'() {
		given:
		Injector injector = Guice.createInjector(new AsyncHttpClientModule())

		when:
		DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

		then:
		client.config.requestFilters.size() == 1
		client.config.requestFilters[0] instanceof CorrelationIdFilter
		client.config.connectTimeout == 5000
		client.config.useInsecureTrustManager == false

		and: 'default overrides are used'
		client.config.requestTimeout == 5000
		client.config.readTimeout == 5000
	}

	def 'override config'() {
		given:
		def overrides = [
			connectTimeout: 100,
			useInsecureTrustManager: true,
			readTimeout: 1000
		]
		Injector injector = Guice.createInjector(new AsyncHttpClientModule(new AsyncHttpClientConfig(properties: overrides)))

		when:
		DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

		then:
		overrides.each { k, v ->
			assert client.config[k] == v
		}
	}

	def 'can add additional request filters'() {
		given:
		RequestFilter filter = Stub()
		Injector injector = Guice.createInjector(new AsyncHttpClientModule(), new AbstractModule() {
			@Override
			protected void configure() {
				def multi = Multibinder.newSetBinder(binder(), RequestFilter)
				multi.addBinding().toInstance(filter)
			}
		})

		when:
		DefaultAsyncHttpClient client = injector.getInstance(AsyncHttpClient)

		then:
		client.config.requestFilters.size() == 2
		client.config.requestFilters.find { it instanceof CorrelationIdFilter }
		client.config.requestFilters.find { it.is(filter) }
	}
}
