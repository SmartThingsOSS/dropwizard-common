package smartthings.dw.guice;

import com.codahale.metrics.health.HealthCheck;

/**
 * If this is used the provided name will be used as the name when
 * things are automatically registered.
 */
public abstract class NamedHealthCheck extends HealthCheck {
	public abstract String getName();
}
