Dropwizard Zipkin
=================
Zipkin integration with dropwizard using `smartthings:dropwizard-guice`.
It also provides some optional Brave instrumentations.
 
## Base module
When installed, the guice module `smartthings.dw.zipkin.ZipkinModule` provides a Brave Tracing instance with a configurable zipkin reporter.  

### Usage:
1. Declare dependency on [`smartthings:dropwizard-zipkin`](https://bintray.com/smartthingsoss/maven/smartthings.dropwizard-zipkin)
2. Add a member variable named `zipkin` of type `smartthings.dw.zipkin.ZipkinConfiguration` to your application configuration class:
    ```java
    import io.dropwizard.Configuration;
    import smartthings.dw.zipkin.ZipkinConfiguration;
    public class MyConfiguration extends Configuration {
        // my other configurations
    	private ZipkinConfiguration zipkin;

    	public ZipkinConfiguration getZipkin() {
    		return zipkin;
    	}

    	public void setZipkin(ZipkinConfiguration zipkin) {
    		this.zipkin = zipkin;
    	}
    }
    ```
3. Add the following to the configuration yaml file:
    ```yaml
    zipkin:
      serviceName: MyAppName
      servicePort: 8080
      sampleRate: 1.0
      reporter:
        type: console
    ```
4. Install `smartthings.dw.zipkin.ZipkinModule` using the zipkin configuration object:
    ```java
    install(new ZipkinModule(myConfiguration.getZipkin()));
    ```
5. Declare dependency on `brave.Tracing` in your class and start tracing:
    ```java
    public class MyService {
        @Inject
        public MyService(Tracing tracing) {
            // trace something
        }
    }
    ```
6. (Optional) To Trace JAX-RS received request and server response, register `smartthings.dw.jersey.ZipkinJerseyEnvironmentCallback`.
    The tracer will join or start new spans on receiving requests and annotate them with `sr` and `ss`.
    ```java
    registerEnvironmentCallback(ZipkinJerseyEnvironmentCallback.class);
    ```

### Using a customized reporter
This integration only supports three zipkin reporter type out of the box: `empty`(`zipkin.reporter.Reporter.NOOP`), `console`(`zipkin.reporter.Reporter.CONSOLE`) and `sqs`.
You can create your own `smartthings.dw.zipkin.SpanReporterFactory` to support other zipkin reporters because `SpanReporterFactory` implements `io.dropwizard.jackson.Discoverable`:   
1. Create a new Factory class that implements `smartthings.dw.zipkin.SpanReporterFactory`
2. Annotate it with `com.fasterxml.jackson.annotation.JsonTypeName` and a unique type name
3. Add the fully qualified class name of the factory to file `META-INF/services/smartthings.dw.zipkin.SpanReporterFactory`

For a detailed writeup on dropwizard's polymorphic configuration, see [polymorphic-configuration docs](http://dropwizard.readthedocs.io/en/latest/manual/configuration.html#polymorphic-configuration).

## Traced AsyncHttpClient module
This is an optional integration of `smartthings.brave:smartthings-brave-asynchttpclient-2.x` and `smartthings:dropwizard-async-http-client`.
When installed, the guice module `smartthings.dw.asynchttpclient.ZipkinAsyncHttpClientModule` provides a brave-instrumented instance of `org.asynchttpclient.AsyncHttpClient`.

### Usage:
1. Declare dependency on [`smartthings.brave:smartthings-brave-asynchttpclient-2.x`](https://bintray.com/smartthingsoss/maven/smartthings.brave)
2. Install `smartthings.dw.asynchttpclient.ZipkinAsyncHttpClientModule`, you can optionally provide an instance of `smartthings.dw.asynchttpclient.AsyncHttpClientConfig` to configure the AHC:
    ```java
    install(new ZipkinAsyncHttpClientModule());
    // OR
    install(new ZipkinAsyncHttpClientModule(myConfiguration.getAsyncHttpClient()));
    ```
3. Remove any existing installation of `smartthings.dw.asynchttpclient.AsyncHttpClientModule`
4. Follow usage of `dropwizard-async-http-client`

## Traced Cassandra module
This is an optional integration of `smartthings.brave:smartthings-brave-cassandra-common` and `smartthings:dropwizard-cassandra`.
When installed, the guice module `smartthings.dw.cassandra.ZipkinCassandraModule` provides a brave-instrumented Cassandra session.

### Usage:
1. Declare dependency on [`smartthings.brave:smartthings-brave-cassandra-common`](https://bintray.com/smartthingsoss/maven/smartthings.brave)
2. Install `smartthings.dw.cassandra.ZipkinCassandraModule` with a `smartthings.dw.cassandra.CassandraConfiguration`:
    ```java
    install(new ZipkinCassandraModule(myConfiguration.getCassandra()));
    ```
3. Remove any existing installation of `smartthings.dw.cassandra.CassandraModule`
4. Follow usage of `dropwizard-cassandra`
