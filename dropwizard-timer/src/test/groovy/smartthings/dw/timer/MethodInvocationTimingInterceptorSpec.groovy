package smartthings.dw.timer

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.annotation.Timed
import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import spock.lang.Specification
import spock.lang.Unroll

import static com.google.inject.matcher.Matchers.annotatedWith
import static com.google.inject.matcher.Matchers.any


class MethodInvocationTimingInterceptorSpec extends Specification {

    @Unroll
    def "interceptor times method with annotation"() {
        given:
        MetricRegistry registry = Mock()
        com.codahale.metrics.Timer timer = Mock()
        com.codahale.metrics.Timer.Context context = Mock()
        Injector injector = Guice.createInjector(new TestModule(registry))
        TimerTestClass testClass = injector.getInstance(TimerTestClass)

        when:
        testClass."${methodName}"()

        then:
        1 * registry.timer(metricName) >> timer
        1 * timer.time() >> context
        1 * context.stop()
        0 * _

        where:
        methodName                      |   metricName
        "timedMethod"                   |   'smartthings.dw.timer.MethodInvocationTimingInterceptorSpec$TimerTestClass.timedMethod'
        "timedNamedMethod"              |   'smartthings.dw.timer.MethodInvocationTimingInterceptorSpec$TimerTestClass.methodName'
        "timedNamedAndAbsoluteMethod"   |   'methodName'
    }

    def "interceptor does not time method without annotation"() {
        given:
        MetricRegistry registry = Mock()
        Injector injector = Guice.createInjector(new TestModule(registry))
        TimerTestClass testClass = injector.getInstance(TimerTestClass)

        when:
        testClass.untimedMethod()

        then:
        0 * _
    }


    private static class TimerTestClass {

        @Timed
        @SuppressWarnings(['EmptyMethod'])
        void timedMethod() {
        }

        @SuppressWarnings(['EmptyMethod'])
        void untimedMethod() {
        }

        @Timed(name = "methodName")
        @SuppressWarnings(['EmptyMethod'])
        void timedNamedMethod() {
        }

        @Timed(name = "methodName", absolute = true)
        @SuppressWarnings(['EmptyMethod'])
        void timedNamedAndAbsoluteMethod() {
        }

    }

    private class TestModule implements Module {

        MetricRegistry registry

        TestModule(MetricRegistry registry) {
            this.registry = registry
        }

        @Override
        void configure(Binder binder) {
            MethodInvocationTimingInterceptor interceptor = new MethodInvocationTimingInterceptor()
            interceptor.setRegistry(this.registry)
            binder.bindInterceptor(any(), annotatedWith(Timed), interceptor)

            binder.bind(TimerTestClass)
        }
    }

}
