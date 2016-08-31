package smartthings.dw.logging;

import com.google.inject.Scopes;
import smartthings.dw.guice.AbstractDwModule;
import smartthings.dw.guice.ServletRegistration;

public class LoggingModule extends AbstractDwModule {
	@Override
	protected void configure() {
		bind(LoggingFilter.class).in(Scopes.SINGLETON);
		registerServletFilter(new ServletRegistration("loggingFilter", LoggingFilter.class));
	}
}
