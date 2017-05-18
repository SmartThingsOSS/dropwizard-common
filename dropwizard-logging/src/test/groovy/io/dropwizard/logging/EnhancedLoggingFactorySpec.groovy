package io.dropwizard.logging

import com.codahale.metrics.MetricRegistry
import com.google.common.collect.ImmutableList
import io.dropwizard.logging.turbo.TurboFilterFactory
import spock.lang.Specification

class EnhancedLoggingFactorySpec extends Specification {

    def 'build configured turbo filters'() {
        given:
        MetricRegistry metricRegistry = Mock(MetricRegistry)
        TurboFilterFactory filterFactory = Mock(TurboFilterFactory)
        String name = "test-logging"
        EnhancedLoggingFactory factory = new EnhancedLoggingFactory()
        factory.setTurboFilters(ImmutableList.of(filterFactory))

        when:
        factory.configure(metricRegistry, name)

        then:
        1 * filterFactory.build()
    }
}
