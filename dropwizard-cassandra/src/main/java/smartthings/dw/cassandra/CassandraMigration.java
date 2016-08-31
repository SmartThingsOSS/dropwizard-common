package smartthings.dw.cassandra;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.migration.MigrationParameters;
import smartthings.migration.MigrationRunner;

public class CassandraMigration {
	private final static Logger LOG = LoggerFactory.getLogger(CassandraMigration.class);

	private CassandraMigration() {
	}

	public static void migrate(CassandraConfiguration config) throws Exception {
		if (config.getAutoMigrate()) {
			LOG.info("Auto migrating cassandra tables in keyspace '{}'", config.getKeyspace());
			LOG.info("Using migration file '{}'", config.getMigrationFile());
			MigrationRunner migrationRunner = new MigrationRunner();

			MigrationParameters.Builder builder = new MigrationParameters.Builder()
				.setKeyspace(config.getKeyspace())
				.setMigrationsLogFile(config.getMigrationFile());

			String seed = config.getSeeds().get(0);

			if (seed.contains(":")) {
				String[] tokens = seed.split(":");
				LOG.info("Using migration seed of '{}' '{}'", tokens[0], tokens[1]);
				builder.setHost(tokens[0]);
				builder.setPort(Integer.parseInt(tokens[1]));
			} else {
				LOG.info("Using migration seed of '{}'", seed);
				builder.setHost(seed);
			}

			if (! Strings.isNullOrEmpty(config.getUser()) && ! Strings.isNullOrEmpty(config.getPassword())) {
				LOG.info("Setting user name '{}' for migration", config.getUser());
				builder.setUsername(config.getUser()).setPassword(config.getPassword());
			}

			if (config.getTruststore() != null && ! Strings.isNullOrEmpty(config.getTruststore().getPath())) {
				LOG.info("Setting trust store path '{}' for migration", config.getTruststore().getPath());
				builder.setTruststorePath(config.getTruststore().getPath())
					.setTruststorePassword(config.getTruststore().getPassword())
					.setKeystorePath(config.getKeystore().getPath())
					.setKeystorePassword(config.getKeystore().getPassword());
			}

			MigrationParameters parameters = builder.build();
			migrationRunner.run(parameters);
		} else {
			LOG.info("Not Migrating. Auto migration not active.");
		}
	}
}
