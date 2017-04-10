package smartthings.dw.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
import io.dropwizard.logging.filter.FilterFactory;

import java.util.List;

@JsonTypeName("dynamic")
public class DynamicThresholdLoggerFilterFactory implements FilterFactory<LoggingEvent> {

    @JsonProperty
    private String defaultThreshold = null;

    @JsonProperty
    private String onHigherOrEqual = null;

    @JsonProperty
    private String onLower = null;

    @JsonProperty
    private List<String> loggers = Lists.newArrayList();

    public String getDefaultThreshold() {
        return defaultThreshold;
    }

    public void setDefaultThreshold(String defaultThreshold) {
        this.defaultThreshold = defaultThreshold;
    }

    public String getOnHigherOrEqual() {
        return onHigherOrEqual;
    }

    public void setOnHigherOrEqual(String onHigherOrEqual) {
        this.onHigherOrEqual = onHigherOrEqual;
    }

    public String getOnLower() {
        return onLower;
    }

    public void setOnLower(String onLower) {
        this.onLower = onLower;
    }

    public List<String> getLoggers() {
        return loggers;
    }

    public void setLoggers(List<String> loggers) {
        this.loggers = loggers;
    }

    @Override
    public Filter<LoggingEvent> build() {
        final DynamicThresholdLoggerFilter filter = new DynamicThresholdLoggerFilter();
        // DynamicThresholdLoggerFilter has defaults for defaultThreshold, onHigherOrEqual and onLower
        if (defaultThreshold != null) {
            filter.setDefaultThreshold(Level.toLevel(defaultThreshold));
        }
        if (onHigherOrEqual != null) {
            filter.setOnHigherOrEqual(FilterReply.valueOf(onHigherOrEqual));
        }
        if (onLower != null) {
            filter.setOnLower(FilterReply.valueOf(onLower));
        }
        loggers.forEach(filter::addLogger);
        filter.start();
        return filter;
    }
}
