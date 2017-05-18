package io.dropwizard.logging.turbo;

import ch.qos.logback.classic.turbo.TurboFilter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

/**
 * A service provider interface for creating Logback {@link TurboFilter} instances.
 * <p/>
 * To create your own, just:
 * <ol>
 * <li>Create a class which implements {@link TurboFilterFactory}.</li>
 * <li>Annotate it with {@code @JsonTypeName} and give it a unique type name.</li>
 * <li>add a {@code META-INF/services/io.dropwizard.logging.turbo.TurboFilterFactory} file with your
 * implementation's full class name to the class path.</li>
 * </ol>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface TurboFilterFactory extends Discoverable {

    TurboFilter build();
}