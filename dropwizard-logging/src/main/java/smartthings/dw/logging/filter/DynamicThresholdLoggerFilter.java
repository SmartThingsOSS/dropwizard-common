package smartthings.dw.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.DynamicThresholdFilter;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is an appender-attached-filter version of the turbo filter {@link DynamicThresholdFilter} with
 * some logger name filtering.
 * Usage:
 * <pre>
 &lt;appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender"&gt;
 &lt;filter class="smartthings.logging.DynamicThresholdLoggerFilter"&gt;
 &lt;DefaultThreshold&gt;INFO&lt;/DefaultThreshold&gt;
 &lt;OnHigherOrEqual&gt;ACCEPT&lt;/OnHigherOrEqual&gt;
 &lt;OnLower&gt;DENY&lt;/OnLower&gt;
 &lt;Logger&gt;smartthings&lt;/Logger&gt;
 &lt;/filter&gt;
 &lt;filter class="ch.qos.logback.classic.filter.ThresholdFilter"&gt;
 &lt;level&gt;INFO&lt;/level&gt;
 &lt;/filter&gt;
 &lt;/appender&gt;
 * </pre>
 */
public class DynamicThresholdLoggerFilter extends Filter<LoggingEvent> {

    public static final String MDC_KEY = "dynamicLogLevel";

    private final Set<String> loggerSet = new LinkedHashSet<>();
    private Level defaultThreshold = Level.ERROR;

    private FilterReply onHigherOrEqual = FilterReply.ACCEPT;
    private FilterReply onLower = FilterReply.DENY;

    private boolean hasLoggerOrAncestor(String target) {
        if (loggerSet.contains(target)) {
            return true;
        } else {
            for(String logger: loggerSet) {
                if (target.startsWith(logger)) {
                    return true;
                }
            }
            return false;

        }
    }

    public void addLogger(String loggerName) {
        if (hasLoggerOrAncestor(loggerName)) {
            addError("Logger " + loggerName + " or an ancestor logger has already been set");
        } else {
            loggerSet.add(loggerName);
        }
    }

    public Level getDefaultThreshold() {
        return defaultThreshold;
    }

    public void setDefaultThreshold(Level defaultThreshold) {
        this.defaultThreshold = defaultThreshold;
    }

    public FilterReply getOnHigherOrEqual() {
        return onHigherOrEqual;
    }

    public void setOnHigherOrEqual(FilterReply onHigherOrEqual) {
        this.onHigherOrEqual = onHigherOrEqual;
    }

    public FilterReply getOnLower() {
        return onLower;
    }

    public void setOnLower(FilterReply onLower) {
        this.onLower = onLower;
    }

    @Override
    public void start() {
        if (this.loggerSet.isEmpty()) {
            addError("No logger was specified");
        }
        super.start();
    }

    @Override
    public FilterReply decide(LoggingEvent event) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }

        if (!hasLoggerOrAncestor(event.getLoggerName())) {
            return FilterReply.NEUTRAL;
        }

        String mdcValue = event.getMDCPropertyMap().get(MDC_KEY);

        Level levelAssociatedWithMDCValue = null;
        if (mdcValue != null) {
            levelAssociatedWithMDCValue = Level.toLevel(mdcValue);
        }
        if (levelAssociatedWithMDCValue == null) {
            levelAssociatedWithMDCValue = defaultThreshold;
        }
        if (event.getLevel().isGreaterOrEqual(levelAssociatedWithMDCValue)) {
            return onHigherOrEqual;
        } else {
            return onLower;
        }
    }
}
