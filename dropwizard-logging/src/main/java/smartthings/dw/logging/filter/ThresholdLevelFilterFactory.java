package smartthings.dw.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.filter.FilterFactory;

import javax.validation.constraints.NotNull;

@JsonTypeName("threshold")
public class ThresholdLevelFilterFactory extends io.dropwizard.logging.filter.ThresholdLevelFilterFactory
    implements FilterFactory<ILoggingEvent> {

    @NotNull
    @JsonProperty
    private String level;

    @Override
    public Filter<ILoggingEvent> build() {
        Filter<ILoggingEvent> filter = super.build(Level.toLevel(level));
        filter.start();
        return filter;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
