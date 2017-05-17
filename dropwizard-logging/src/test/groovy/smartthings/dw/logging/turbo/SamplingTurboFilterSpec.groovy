package smartthings.dw.logging.turbo

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import smartthings.dw.logging.turbo.SamplingTurboFilter
import spock.lang.Specification

class SamplingTurboFilterSpec extends Specification {
    SamplingTurboFilter filter
    Logger logger
    Marker samplingMarker
    Objects[] objects


    def setup() {
        logger = new Logger("test", null, null)
        samplingMarker = SamplingTurboFilter.SAMPLING_MARKER
        objects = []
    }

    def "filter should return DENY if filter sample rate is 0" () {
        given:
        filter = new SamplingTurboFilter()
        filter.setSampleRate(0)
        filter.start()

        when:
        def reply = filter.decide(samplingMarker, logger, Level.INFO, "testing", objects, null)

        then:
        reply == FilterReply.DENY
    }

    def "filter should return NEUTRAL if filter sample rate is 1" () {
        given:
        filter = new SamplingTurboFilter()
        filter.setSampleRate(1)
        filter.start()

        when:
        def reply = filter.decide(samplingMarker, logger, Level.INFO, "testing", objects, null)

        then:
        reply == FilterReply.NEUTRAL
    }

    def "filter should return NEUTRAL if marker does not match" () {
        given:
        filter = new SamplingTurboFilter()
        filter.setSampleRate(0)
        filter.start()

        when:
        def reply = filter.decide(marker, logger, Level.INFO, "testing", objects, null)

        then:
        reply == FilterReply.NEUTRAL

        where:
        marker << [ MarkerFactory.getMarker("foo"), null ]
    }
}
