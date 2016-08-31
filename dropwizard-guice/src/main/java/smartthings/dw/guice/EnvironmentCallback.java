package smartthings.dw.guice;

import io.dropwizard.setup.Environment;

/**
 * Allows modules to register classes that work directly on an environment
 */
public interface EnvironmentCallback {

	default void postSetup(Environment environment) {}
}
