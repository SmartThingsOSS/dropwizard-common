Dropwizard Cassandra
====================
Cassandra driver integrations with dropwizard using `smartthings:dropwizard-guice`.
When installed, the guice module `smartthigs.dw.cassandra.CassandraModule` provides:
* a configurable cluster connection and `com.datastax.driver.core.Session`
* a Dropwizard-managed cluster connection lifecycle with health check
* a `com.datastax.driver.mapping.MappingManager` for using `com.datastax.cassandra:cassandra-driver-mapping`

### Usage:
1. Declare dependency on [`smartthings:dropwizard-cassandra`](https://bintray.com/smartthingsoss/maven/smartthings.dropwizard-cassandra)
2. Add a member variable named `cassandra` of type `smartthings.dw.cassandra.CassandraConfiguration` to your application configuration class:
    ```java
    import io.dropwizard.Configuration;
    import smartthings.dw.cassandra.CassandraConfiguration;
    public class MyConfiguration extends Configuration {
        // my other configurations
        private CassandraConfiguration cassandra;

        public CassandraConfiguration getCassandra() {
            return cassandra;
        }

        public void setCassandra(CassandraConfiguration cassandra) {
            this.cassandra = cassandra;
        }
    }
    ```
3. Add the following to the configuration yaml file:
    ```yaml
    cassandra:
      keyspace: "ticker"
      seeds:
        - localhost
      autoMigrate: true
      migrationFile: "/migrations/cql.changelog"
      queryOptions:
        consistencyLevel: LOCAL_QUORUM
        serialConsistencyLevel: LOCAL_SERIAL
        fetchSize: 1000
      retryPolicy:
        type: loggingRetry
        subPolicy:
          type: downgradingConsistencyRetry
      loadBalancingPolicy:
        type: tokenAware
        subPolicy:
          type: dcAwareRoundRobin
    ```
    For the complete list of configuration keys, see source of [`smartthings.dw.cassandra.CassandraConfiguration`](https://github.com/SmartThingsOSS/dropwizard-common/blob/master/dropwizard-cassandra/src/main/java/smartthings/dw/cassandra/CassandraConfiguration.java).
4. Install `smartthings.dw.cassandra.CassandraModule`:
    ```java
    install(new CassandraModule(myConfiguration.getCassandra()));
    ```
5. Declare dependency on `com.datastax.driver.core.Session` in your class and start executing statements:
    ```java
    public class MyService {
            @Inject
            public MyService(Session session) {
                // write or read something
            }
        }
    ```
