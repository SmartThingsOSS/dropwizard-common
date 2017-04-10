package smartthings.dw.logging.filter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import spock.lang.Specification

class ThresholdLevelFilterFactorySpec extends Specification {

    ThresholdLevelFilterFactory factory = new ThresholdLevelFilterFactory()

    def "build using level"() {
        when:
        factory.setLevel("DEBUG")
        Filter<ILoggingEvent> filter = factory.build()

        then:
        filter instanceof ThresholdFilter
        ((ThresholdFilter) filter).level == Level.DEBUG
    }
}
