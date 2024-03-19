package smartthings.dw.cassandra;

import com.datastax.driver.core.*;
import com.google.common.base.Strings;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.cassandra.addresstranslator.AddressTranslatorFactory;
import smartthings.dw.cassandra.loadbalancing.LoadBalancingPolicyFactory;
import smartthings.dw.cassandra.retry.RetryPolicyFactory;
import smartthings.dw.cassandra.speculative.SpeculativeExecutionPolicyFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.validation.Valid;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CassandraConfiguration {
	private final static Logger LOG = LoggerFactory.getLogger(CassandraConfiguration.class);

	private String[] cipherSuites = new String[]{"TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA"};

	private JKSConfig truststore;
	private JKSConfig keystore;

	private String user;
	private String password;

	@NotEmpty
	private String keyspace;

	private String migrationFile = "/migrations/cql.changelog";
	private Boolean autoMigrate = false;

	private QueryOptions queryOptions;
    private PoolConfiguration pooling;

	@Valid
	private RetryPolicyFactory retryPolicy;

	@Valid
	private SpeculativeExecutionPolicyFactory speculativeExecutionPolicy;

	@Valid
	private LoadBalancingPolicyFactory loadBalancingPolicy;

	@Valid
	private AddressTranslatorFactory addressTranslatorFactory;

	private List<String> seeds;

	private int protocolVersion = 0;

    private Long shutdownTimeoutInMillis = TimeUnit.SECONDS.toMillis(30);

    private String testQuery = "SELECT cql_version FROM system.local LIMIT 1";

	public JKSConfig getTruststore() {
		return truststore;
	}

	public void setTruststore(JKSConfig truststore) {
		this.truststore = truststore;
	}

	public JKSConfig getKeystore() {
		return keystore;
	}

	public void setKeystore(JKSConfig keystore) {
		this.keystore = keystore;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getKeyspace() {
		return keyspace;
	}

	public void setKeyspace(String keyspace) {
		this.keyspace = keyspace;
	}

	public List<String> getSeeds() {
		return seeds;
	}

	public void setSeeds(List<String> seeds) {
		this.seeds = seeds;
	}

	public RegularStatement getValidationQuery() {
	    SimpleStatement statement = new  SimpleStatement(testQuery);
	    statement.setIdempotent(true);
	    return statement;
	}

	public String getMigrationFile() {
		return migrationFile;
	}

	public void setMigrationFile(String migrationFile) {
		this.migrationFile = migrationFile;
	}

	public Boolean getAutoMigrate() {
		return autoMigrate;
	}

	public void setAutoMigrate(Boolean autoMigrate) {
		this.autoMigrate = autoMigrate;
	}

	public QueryOptions getQueryOptions() {
		return queryOptions;
	}

	public void setQueryOptions(QueryOptions queryOptions) {
		this.queryOptions = queryOptions;
	}

	public RetryPolicyFactory getRetryPolicy() {
		return retryPolicy;
	}

	public void setRetryPolicy(RetryPolicyFactory retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public SpeculativeExecutionPolicyFactory getSpeculativeExecutionPolicy() {
		return speculativeExecutionPolicy;
	}

	public void setSpeculativeExecutionPolicy(SpeculativeExecutionPolicyFactory speculativeExecutionPolicy) {
		this.speculativeExecutionPolicy = speculativeExecutionPolicy;
	}

	public LoadBalancingPolicyFactory getLoadBalancingPolicy() {
		return loadBalancingPolicy;
	}

	public void setLoadBalancingPolicy(LoadBalancingPolicyFactory loadBalancingPolicy) {
		this.loadBalancingPolicy = loadBalancingPolicy;
	}

	public AddressTranslatorFactory getAddressTranslatorFactory() {
		return addressTranslatorFactory;
	}

	public void setAddressTranslatorFactory(AddressTranslatorFactory addressTranslatorFactory) {
		this.addressTranslatorFactory = addressTranslatorFactory;
	}

    public Long getShutdownTimeoutInMillis() {
        return shutdownTimeoutInMillis;
    }

    public void setShutdownTimeoutInMillis(Long shutdownTimeoutInMillis) {
        this.shutdownTimeoutInMillis = shutdownTimeoutInMillis;
    }

    public String getTestQuery() {
        return testQuery;
    }

    public void setTestQuery(String testQuery) {
        this.testQuery = testQuery;
    }

    public int getProtocolVersion() {
	    return this.protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
	    this.protocolVersion = protocolVersion;
    }

    public PoolConfiguration getPooling() {
        return pooling;
    }

    public void setPooling(PoolConfiguration pooling) {
        this.pooling = pooling;
    }

    public static class JKSConfig {

		String path;
		String password;

		public JKSConfig() {
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public Session buildSession() {
		Session session;
		Cluster cluster;

		Cluster.Builder builder = Cluster.builder();

		if (loadBalancingPolicy != null) {
			builder.withLoadBalancingPolicy(loadBalancingPolicy.build());
		}

		if (speculativeExecutionPolicy != null) {
			builder.withSpeculativeExecutionPolicy(speculativeExecutionPolicy.build());
		}

		if (addressTranslatorFactory != null) {
			builder.withAddressTranslator(addressTranslatorFactory.build());
		}

		if (queryOptions != null) {
			LOG.info("QueryOptions - ConsistencyLevel {}", queryOptions.getConsistencyLevel());
			LOG.info("QueryOptions - SerialConsistencyLevel {}", queryOptions.getSerialConsistencyLevel());
			LOG.info("QueryOptions - FetchSize {}", queryOptions.getFetchSize());
			LOG.info("QueryOptions - DefaultIdempotence {}", queryOptions.getDefaultIdempotence());
			builder.withQueryOptions(queryOptions);
		}

        if (pooling != null) {
            LOG.info("PoolOptions - {}", pooling);
            builder.withPoolingOptions(pooling.toPoolOptions());
        }

		if (retryPolicy != null) {
			builder.withRetryPolicy(retryPolicy.build());
		}

		for (String seed : seeds) {
			if (seed.contains(":")) {
				String[] tokens = seed.split(":");
				LOG.info("Seed - {}:{}", tokens[0], tokens[1]);
				builder.addContactPoint(tokens[0]).withPort(Integer.parseInt(tokens[1]));
			} else {
				LOG.info("Seed - {}", seed);
				builder.addContactPoint(seed);
			}
		}

		if (truststore != null && ! Strings.isNullOrEmpty(truststore.getPath())) {
			try {
				LOG.info("With SSL");
				SSLContext sslContext = getSSLContext(truststore.path, truststore.password, keystore.path, keystore.password);
				builder.withSSL(new JdkSSLOptions.Builder()
                    .withSSLContext(sslContext)
                    .withCipherSuites(cipherSuites)
                    .build());
			} catch (Exception e) {
				LOG.error("Couldn't add SSL to the cluster builder.", e);
			}
		}

		if (! Strings.isNullOrEmpty(user) && ! Strings.isNullOrEmpty(password)) {
			LOG.info("Credentials - {}", user);
			builder.withCredentials(user, password);
		}

		if (protocolVersion != 0) {
		    builder.withProtocolVersion(ProtocolVersion.fromInt(protocolVersion));
		    LOG.info("Protocol Version - {}", protocolVersion);
        }

		cluster = builder.build();

		LOG.info("Keyspace - {}", keyspace);
		session = cluster.connect(keyspace);
		LOG.info("Connection Created - {}", session);
		return session;
	}

	private static SSLContext getSSLContext(String truststorePath, String truststorePassword, String keystorePath, String keystorePassword) throws Exception {
		FileInputStream tsf = new FileInputStream(truststorePath);
		FileInputStream ksf = new FileInputStream(keystorePath);
		SSLContext ctx = SSLContext.getInstance("SSL");

		KeyStore ts = KeyStore.getInstance("JKS");
		ts.load(tsf, truststorePassword.toCharArray());
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(ts);

		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(ksf, keystorePassword.toCharArray());
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

		kmf.init(ks, keystorePassword.toCharArray());

		ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
		return ctx;
	}

    public static class PoolConfiguration {
        private Integer idleTimeoutSeconds;
        private Integer heartbeatIntervalSeconds;
        private Integer maxQueueSize;
        private Integer poolTimeoutMillis;
        private Integer localMaxConnPerHost;
        private Integer remoteMaxConnPerHost;
        private Integer localMaxRequestsPerConn;
        private Integer remoteMaxRequestsPerConn;

        public PoolConfiguration() {

        }

        public Integer getIdleTimeoutSeconds() {
            return idleTimeoutSeconds;
        }

        public void setIdleTimeoutSeconds(Integer idleTimeoutSeconds) {
            this.idleTimeoutSeconds = idleTimeoutSeconds;
        }

        public Integer getHeartbeatIntervalSeconds() {
            return heartbeatIntervalSeconds;
        }

        public void setHeartbeatIntervalSeconds(Integer heartbeatIntervalSeconds) {
            this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
        }

        public Integer getMaxQueueSize() {
            return maxQueueSize;
        }

        public void setMaxQueueSize(Integer maxQueueSize) {
            this.maxQueueSize = maxQueueSize;
        }

        public Integer getPoolTimeoutMillis() {
            return poolTimeoutMillis;
        }

        public void setPoolTimeoutMillis(Integer poolTimeoutMillis) {
            this.poolTimeoutMillis = poolTimeoutMillis;
        }

        public Integer getLocalMaxConnPerHost() {
            return localMaxConnPerHost;
        }

        public void setLocalMaxConnPerHost(Integer localMaxConnPerHost) {
            this.localMaxConnPerHost = localMaxConnPerHost;
        }

        public Integer getRemoteMaxConnPerHost() {
            return remoteMaxConnPerHost;
        }

        public void setRemoteMaxConnPerHost(Integer remoteMaxConnPerHost) {
            this.remoteMaxConnPerHost = remoteMaxConnPerHost;
        }

        public Integer getLocalMaxRequestsPerConn() {
            return localMaxRequestsPerConn;
        }

        public void setLocalMaxRequestsPerConn(Integer localMaxRequestsPerConn) {
            this.localMaxRequestsPerConn = localMaxRequestsPerConn;
        }

        public Integer getRemoteMaxRequestsPerConn() {
            return remoteMaxRequestsPerConn;
        }

        public void setRemoteMaxRequestsPerConn(Integer remoteMaxRequestsPerConn) {
            this.remoteMaxRequestsPerConn = remoteMaxRequestsPerConn;
        }

        @Override
        public String toString() {
            return "PoolConfiguration{" +
                "idleTimeoutSeconds=" + idleTimeoutSeconds +
                ", heartbeatIntervalSeconds=" + heartbeatIntervalSeconds +
                ", maxQueueSize=" + maxQueueSize +
                ", poolTimeoutMillis=" + poolTimeoutMillis +
                ", localMaxConnPerHost=" + localMaxConnPerHost +
                ", remoteMaxConnPerHost=" + remoteMaxConnPerHost +
                ", localMaxRequestsPerConn=" + localMaxRequestsPerConn +
                ", remoteMaxRequestsPerConn=" + remoteMaxRequestsPerConn +
                '}';
        }

        public PoolingOptions toPoolOptions() {
            PoolingOptions options = new PoolingOptions();

            if (idleTimeoutSeconds != null) {
                options.setIdleTimeoutSeconds(idleTimeoutSeconds);
            }

            if (heartbeatIntervalSeconds != null) {
                options.setHeartbeatIntervalSeconds(heartbeatIntervalSeconds);
            }

            if (maxQueueSize != null) {
                options.setMaxQueueSize(maxQueueSize);
            }

            if (poolTimeoutMillis != null) {
                options.setPoolTimeoutMillis(poolTimeoutMillis);
            }

            if (localMaxConnPerHost != null) {
                options.setMaxConnectionsPerHost(HostDistance.LOCAL, localMaxConnPerHost);
            }

            if (remoteMaxConnPerHost != null) {
                options.setMaxConnectionsPerHost(HostDistance.REMOTE, remoteMaxConnPerHost);
            }

            if (localMaxRequestsPerConn != null) {
                options.setMaxRequestsPerConnection(HostDistance.LOCAL, localMaxRequestsPerConn);
            }

            if (remoteMaxRequestsPerConn != null) {
                options.setMaxRequestsPerConnection(HostDistance.REMOTE, remoteMaxRequestsPerConn);
            }
            return options;
        }
    }
}
