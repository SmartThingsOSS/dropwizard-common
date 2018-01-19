Dropwizard AsyncHttpClient
==========================
AsyncHttpClient integration with dropwizard using `smartthings:dropwizard-guice`.
When installed, the guice module `smartthings.dw.asynchttpclient.AsyncHttpClientModule` provides a configured instance of AHC.

### Usage:
1. Declare dependency on [`smartthings:dropwizard-async-http-client`](https://bintray.com/smartthingsoss/maven/smartthings.dropwizard-async-http-client).
2. Add a member variable named `asyncHttpClient` of type `smartthings.dw.asynchttpclient.AsyncHttpClientConfig` to your application configuration class:
    ```java
    import io.dropwizard.Configuration;
    import smartthings.dw.asynchttpclient.AsyncHttpClientConfig;
    public class MyConfiguration extends Configuration {
        // my other configurations
        private AsyncHttpClientConfig asyncHttpClient;

        public AsyncHttpClientConfig getAsyncHttpClient() {
            return asyncHttpClient;
        }

        public void setAsyncHttpClient(AsyncHttpClientConfig asyncHttpClient) {
            this.asyncHttpClient = asyncHttpClient;
        }
    }
    ```
3. Add the following to the configuration yaml file:
    ```yaml
    asyncHttpClient:
        properties:
            connectTimeout: 5000
            acceptAnyCertificate: false
            maxConnections: -1
    ```
    For the complete list of configuration keys, see source of [`org.asynchttpclient.config.AsyncHttpClientConfigDefaults.AsyncHttpClientConfigDefaults`](https://github.com/AsyncHttpClient/async-http-client/blob/master/client/src/main/java/org/asynchttpclient/config/AsyncHttpClientConfigDefaults.java) and their [defaults](https://github.com/AsyncHttpClient/async-http-client/blob/master/client/src/main/resources/ahc-default.properties).
4. Install `smartthings.dw.asynchttpclient.AsyncHttpClientModule`:
    ```java
    install(new AsyncHttpClientModule(myConfiguration.getAsyncHttpClient()));
    ```
5. Declare dependency on `org.asynchttpclient.AsyncHttpClient` in your class and start making http requests:
    ```java
    public class MyService {
            @Inject
            public MyService(AsyncHttpClient httpClient) {
                // request something
            }
        }
    ```
