package smartthings.dw.guice;

import io.dropwizard.setup.Environment;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;

public class ServletRegistration {
	private final String name;
	private final Class<? extends Filter> klass;
	private final RegisterServlet registerServlet;

	public interface RegisterServlet {
		void register(Environment environment, FilterRegistration.Dynamic dynamic);
	}

	private static RegisterServlet DEFAULT_SERVLET_MAPPER =
		(Environment environment, FilterRegistration.Dynamic dynamic) -> {
			dynamic.addMappingForUrlPatterns(
				EnumSet.of(DispatcherType.REQUEST), true, environment.getApplicationContext().getContextPath() + "*"
			);
		};

	public ServletRegistration(String name, Class<? extends Filter> klass, RegisterServlet registerServlet) {
		this.name = name;
		this.klass = klass;
		this.registerServlet = registerServlet;
	}

	public ServletRegistration(String name, Class<? extends Filter> klass) {
		this(name, klass, DEFAULT_SERVLET_MAPPER);
	}

	public void register(Environment environment, FilterRegistration.Dynamic dynamic) {
		registerServlet.register(environment, dynamic);
	}

	public String getName() {
		return name;
	}

	public Class<? extends Filter> getKlass() {
		return klass;
	}
}
