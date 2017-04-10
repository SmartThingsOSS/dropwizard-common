package smartthings.dw.logging.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply
import spock.lang.Specification

class DynamicThresholdLoggerFilterFactorySpec extends Specification{

    DynamicThresholdLoggerFilterFactory factory = new DynamicThresholdLoggerFilterFactory()

    def "build"() {
        when:
        factory.setDefaultThreshold("INFO")
        factory.setOnHigherOrEqual("ACCEPT")
        factory.setOnLower("DENY")
        factory.setLoggers(["smartthings.dw.logging"])
        Filter<ILoggingEvent> filter = factory.build()

        then:
        filter instanceof DynamicThresholdLoggerFilter
        DynamicThresholdLoggerFilter dynamicFilter = (DynamicThresholdLoggerFilter) filter
        dynamicFilter.getDefaultThreshold() == Level.INFO
        dynamicFilter.getOnHigherOrEqual() == FilterReply.ACCEPT
        dynamicFilter.getOnLower() == FilterReply.DENY
        dynamicFilter.loggerSet == ["smartthings.dw.logging"] as Set<String>
    }
}
