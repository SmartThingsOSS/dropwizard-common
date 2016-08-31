package smartthings.dw.guice;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.setup.Environment;

/**
 * This is a utility module which can allow you to inject various items from the
 * environment if used. Note that this is not currently used by default, so you
 * are required to add it to the modules you pass to guice.
 */
public class EnvironmentModule extends AbstractDwModule {

	private final Environment environment;

	public EnvironmentModule(Environment environment) {
		this.environment = environment;
	}

	@Override
	protected void configure() {
		bind(MetricRegistry.class).toInstance(environment.metrics());
		bind(ObjectMapper.class).toInstance(environment.getObjectMapper());
	}
}
