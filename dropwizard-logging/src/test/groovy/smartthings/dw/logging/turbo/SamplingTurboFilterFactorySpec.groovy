package smartthings.dw.logging.turbo

import ch.qos.logback.classic.turbo.TurboFilter
import smartthings.dw.logging.turbo.SamplingTurboFilter
import smartthings.dw.logging.turbo.SamplingTurboFilterFactory
import spock.lang.Specification

class SamplingTurboFilterFactorySpec extends Specification {
    def 'build sampling turbo filter' () {
        given:
        SamplingTurboFilterFactory factory = new SamplingTurboFilterFactory()
        factory.setSampleRate(0.1337)

        when:
        TurboFilter filter = factory.build()

        then:
        filter instanceof SamplingTurboFilter
    }

}
