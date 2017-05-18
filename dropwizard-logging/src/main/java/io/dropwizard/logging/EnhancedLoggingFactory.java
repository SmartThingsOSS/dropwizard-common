package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import io.dropwizard.logging.turbo.TurboFilterFactory;

/**
 * "Enhanced"LoggingFactory is just DefaultLoggingFactory with turbo filters support
 */
@JsonTypeName("enhanced")
public class EnhancedLoggingFactory extends DefaultLoggingFactory {

    @JsonProperty
    private ImmutableList<TurboFilterFactory> turboFilters = ImmutableList.of();

    public void setTurboFilters(ImmutableList<TurboFilterFactory> turboFilters) {
        this.turboFilters = turboFilters;
    }

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
        super.configure(metricRegistry, name);
        LoggerContext loggerContext = getLoggerContext();
        for (TurboFilterFactory factory: turboFilters) {
            loggerContext.addTurboFilter(factory.build());
        }
    }
}
