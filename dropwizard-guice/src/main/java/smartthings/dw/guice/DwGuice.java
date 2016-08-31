package smartthings.dw.guice;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import java.util.List;
import java.util.Set;

/**
 * Utility class to register objects created by guice with dropwizard. General usage
 * is to put code such as below into your main classes run method.
 * <p> {@code
 * DwGuice.from(new MyAppModule(configuration)).register(environment);
 * }</p>
 * <p>Also so {@link EnvironmentModule} which will put some objects from the environment
 * into your guice registry.</p>
 */
public class DwGuice {
	private final static Logger LOG = LoggerFactory.getLogger(DwGuice.class);
	private final Injector injector;

	private DwGuice(Injector injector) {
		this.injector = injector;
	}

	public static DwGuice from(Stage stage, Module... modules) {
		LOG.info("Creating Injector");
		List<Module> ms = Lists.asList(new ServletModule(), modules);
		Injector injector = Guice.createInjector(stage, ms);
		LOG.info("Injector Created");
		return new DwGuice(injector);
	}

	public static DwGuice from(Module... modules) {
		return from(Stage.DEVELOPMENT, modules);
	}

	protected DwGuice registerGuiceFilter(Environment environment) {
		LOG.info("Registering Guice Filter");
		environment.servlets().addFilter("Guice Filter", GuiceFilter.class)
			.addMappingForUrlPatterns(null, false, environment.getApplicationContext().getContextPath() + "*");
		return this;
	}

	protected DwGuice registerHealthChecks(Environment environment) {
		LOG.info("Registering Health Checks");
		try {
			Set<HealthCheck> checks = injector.getInstance(
					Key.get(new TypeLiteral<Set<HealthCheck>>() {
					}));
			checks.forEach(check -> {
				String name;
				if (check instanceof NamedHealthCheck) {
					name = ((NamedHealthCheck) check).getName();
				} else {
					name = check.getClass().getSimpleName();
				}
				LOG.info("Registering health check '{}' - {}", name, check.getClass().getCanonicalName());
				environment.healthChecks().register(name, check);
			});
		} catch (ConfigurationException e) {
			LOG.info("No health checks found to register");
		}
		return this;
	}

	protected DwGuice registerServletFilters(Environment environment) {
		LOG.info("Registering Servlet Filters");
		try {
			Set<ServletRegistration> registrations = injector.getInstance(
				Key.get(new TypeLiteral<Set<ServletRegistration>>() {
				}));
			registrations.forEach(registration -> {
				Filter filter = injector.getInstance(registration.getKlass());
				LOG.info("Registering servlet filter '{}' - {}",
					registration.getName(), filter.getClass().getCanonicalName());
				FilterRegistration.Dynamic dynamic = environment.servlets().addFilter(registration.getName(), filter);
				registration.register(environment, dynamic);

			});
		} catch (ConfigurationException e) {
			LOG.info("No servlet filters found to register");
		}
		return this;
	}

	protected DwGuice registerManaged(Environment environment) {
		LOG.info("Registering Managed Classes");
		try {
			Set<Managed> managed = injector.getInstance(
				Key.get(new TypeLiteral<Set<Managed>>() {
				}));
			managed.forEach(m -> {
				LOG.info("Registering managed class - {}", m.getClass().getCanonicalName());
				environment.lifecycle().manage(m);
			});
		} catch (ConfigurationException e) {
			LOG.info("No managed classes found to register");
		}
		return this;
	}

	protected DwGuice registerResources(Environment environment) {
		LOG.info("Registering Resources");
		try {
			Set<WebResource> resources = injector.getInstance(
				Key.get(new TypeLiteral<Set<WebResource>>() {
				}));
			resources.forEach(r -> {
				LOG.info("Registering resource class - {}", r.getClass().getCanonicalName());
				environment.jersey().register(r);
			});
		} catch (ConfigurationException e) {
			LOG.info("No resources found to register");
		}
		return this;
	}

	protected DwGuice registerTasks(Environment environment) {
		LOG.info("Registering Tasks");
		try {
			Set<Task> tasks = injector.getInstance(
				Key.get(new TypeLiteral<Set<Task>>() {
				}));
			tasks.forEach(task -> {
				LOG.info("Registering task class - {}", task.getClass().getCanonicalName());
				environment.admin().addTask(task);
			});
		} catch (ConfigurationException e) {
			LOG.info("No tasks found to register");
		}
		return this;
	}

	protected DwGuice runPostSetup(Environment environment) {
		LOG.info("Running Post Setup");
		try {
			Set<EnvironmentCallback> callbacks = injector.getInstance(
				Key.get(new TypeLiteral<Set<EnvironmentCallback>>() {
				}));
			callbacks.forEach(callback -> {
				LOG.info("Running postSetup callback for class - {}", callback.getClass().getCanonicalName());
				callback.postSetup(environment);
			});
		} catch (ConfigurationException e) {
			LOG.info("No postSetup callbacks found to run");
		}
		return this;
	}

	public DwGuice register(Environment environment) {
		registerGuiceFilter(environment);
		registerServletFilters(environment);
		registerHealthChecks(environment);
		registerManaged(environment);
		registerResources(environment);
		registerTasks(environment);
		runPostSetup(environment);
		return this;
	}

	public Injector getInjector() {
		return injector;
	}
}
