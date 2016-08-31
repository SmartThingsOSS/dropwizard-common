package smartthings.dw.buildinfo;

import com.google.inject.Scopes;
import smartthings.dw.guice.AbstractDwModule;

public class BuildInfoModule extends AbstractDwModule {
	@Override
	protected void configure() {
		bind(BuildMetadataResource.class).in(Scopes.SINGLETON);
		registerResource(BuildMetadataResource.class);
	}
}
