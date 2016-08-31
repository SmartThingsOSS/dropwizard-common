package smartthings.dw.guice;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;

/**
 * This gives convenience methods for registering many different dropwizard objects from within
 * a module. Registration is done using {@link Multibinder} so we don't have to do any classpath
 * scanning.
 */
public abstract class AbstractDwModule extends AbstractModule {

	/**
	 * Register managed classes so that the dropwizard lifecycle will be applied to them. All
	 * classes should be wired up in guice separately and will be registered in dropwizard as
	 * singletons regardless in how the are wired in guice.
	 */
	@SafeVarargs
	public final void registerManaged(Class<? extends Managed>... klasses) {
		Multibinder<Managed> managedBinder = Multibinder.newSetBinder(binder(), Managed.class);
		for (Class<? extends Managed> klass : klasses) {
			managedBinder.addBinding().to(klass);
		}
	}

	/**
	 * Register dropwizard health check classes. {@code NamedHealthCheck} can be used if you want
	 * to give the health check a name other than the class name. Classes should be wired up in guice
	 * separately and will be registered in dropwizard as singletons regardless in how the are wired
	 * in guice.
	 */
	@SafeVarargs
	public final void registerHealthCheck(Class<? extends HealthCheck>... klasses) {
		Multibinder<HealthCheck> healthBinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
		for (Class<? extends HealthCheck> klass : klasses) {
			healthBinder.addBinding().to(klass);
		}
	}

	/**
	 * Register dropwizard resource classes. {@code WebResource} is a marker interface and is used to
	 * avoid classpath scanning. All classes should be wired up in guice separately and will be registered
	 * in dropwizard as singletons regardless in how the are wired in guice.
	 */
	@SafeVarargs
	public final void registerResource(Class<? extends WebResource>... klasses) {
		Multibinder<WebResource> resourceBinder = Multibinder.newSetBinder(binder(), WebResource.class);
		for (Class<? extends WebResource> klass : klasses) {
			resourceBinder.addBinding().to(klass);
		}
	}

	/**
	 * Register environment callbacks. These will allow plugins to register items directly on the environment.
	 */
	@SafeVarargs
	public final void registerEnvironmentCallback(Class<? extends EnvironmentCallback>... klasses) {
		Multibinder<EnvironmentCallback> callbackBinder = Multibinder.newSetBinder(binder(), EnvironmentCallback.class);
		for (Class<? extends EnvironmentCallback> hook : klasses) {
			callbackBinder.addBinding().to(hook);
		}
	}

	/**
	 * Register a servlet filter in dropwizard.  Unlike the other methods here that take only a class this
	 * requires more since more information is needed upon registration. It is best not to register the passed
	 * in {@code ServletRegistration} in guice, but you should register the klass object referenced in the
	 * {@code ServletRegistration}. All filters will be built by guice and in dropwizard as singletons
	 * regardless in how the are wired in guice.
	 */
	public final void registerServletFilter(ServletRegistration... servletRegistrations) {
		Multibinder<ServletRegistration> filterBinder = Multibinder.newSetBinder(binder(), ServletRegistration.class);
		for (ServletRegistration registration : servletRegistrations) {
			filterBinder.addBinding().toInstance(registration);
		}
	}

	/**
	 * Register a task in dropwizard. Classes should be wired up in guice separately and will be registered
	 * in dropwizard as singletons regardless in how the are wired in guice.
	 */
	@SafeVarargs
	public final void registerTask(Class<? extends Task>... klasses) {
		Multibinder<Task> taskBinder = Multibinder.newSetBinder(binder(), Task.class);
		for (Class<? extends Task> klass : klasses) {
			taskBinder.addBinding().to(klass);
		}
	}
}
