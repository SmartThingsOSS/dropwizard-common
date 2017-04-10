package smartthings.dw.logging.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Context
import ch.qos.logback.core.spi.FilterReply
import spock.lang.Specification

class DynamicThresholdLoggerFilterSpec extends Specification {

    FilterReply onLower = FilterReply.DENY
    FilterReply onHigherOrEqual = FilterReply.ACCEPT
    Level defaultThreshold = Level.WARN
    DynamicThresholdLoggerFilter filter
    Context context
    LoggingEvent event

    def setup() {
        filter = new DynamicThresholdLoggerFilter()
        context = Mock(LoggerContext)
        event = Mock(LoggingEvent)
        filter.setContext(context)
        filter.setOnLower(onLower)
        filter.setOnHigherOrEqual(onHigherOrEqual)
        filter.setDefaultThreshold(defaultThreshold)
    }

    def "decide should return NEUTRAL if no logger was set" () {
        given:
        filter.start()

        when:
        FilterReply fr = filter.decide(event)

        then:
        fr == FilterReply.NEUTRAL
        1 * event.getLoggerName() >> "foo.bar"
        0 * _
    }

    def "decide should return NEUTRAL if event logger does not match any filter logger" () {
        given:
        filter.addLogger("xyz")
        filter.start()

        when:
        FilterReply fr = filter.decide(event)

        then:
        fr == FilterReply.NEUTRAL
        1 * event.getLoggerName() >> "foo.bar"
        0 * _
    }

    def "decide should return ACCEPT if event level is equal to level associated with MDC value" () {
        given:
        filter.addLogger("foo")
        filter.start()

        when:
        FilterReply fr = filter.decide(event)

        then:
        fr == FilterReply.ACCEPT
        1 * event.getLoggerName() >> "foo.bar"
        1 * event.getMDCPropertyMap() >> [dynamicLogLevel: "DEBUG"]
        1 * event.getLevel() >> Level.DEBUG
        0 * _
    }
}
