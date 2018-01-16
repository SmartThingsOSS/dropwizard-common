package smartthings.dw.successmeter

import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.google.inject.Binder
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import spock.lang.Specification
import spock.lang.Unroll

import static com.google.inject.matcher.Matchers.annotatedWith
import static com.google.inject.matcher.Matchers.any


class MethodInvocationSuccessMeterInterceptorSpec extends Specification {

    @Unroll
    def "interceptor gathers metrics for method with annotation"() {
        given:
        MetricRegistry registry = Mock(MetricRegistry)
        Meter meter = Mock(Meter)
        Injector injector = Guice.createInjector(new TestModule(registry))
        SuccessMeterTestClass testClass = injector.getInstance(SuccessMeterTestClass)

        when:
        try {
            testClass."${methodName}"()
        } catch (RuntimeException e) {

        }

        then:
        1 * registry.meter(metricName) >> meter
        1 * meter.mark()
        0 * _

        where:
        methodName      | metricName
        "successMethod" | 'smartthings.dw.successmeter.MethodInvocationSuccessMeterInterceptorSpec$SuccessMeterTestClass.successMethod.success'
        "failedMethod"  | 'Pavement.IstRad'
        "anotherSuccessMethod"  | 'Pavement2.ItIsGood'
    }

    def "interceptor does not collect metrics on method without annotation"() {
        given:
        MetricRegistry registry = Mock(MetricRegistry)
        Injector injector = Guice.createInjector(new TestModule(registry))
        SuccessMeterTestClass testClass = injector.getInstance(SuccessMeterTestClass)

        when:
        testClass.noAnnotationMethod()

        then:
        0 * _
    }


    private static class SuccessMeterTestClass {

        @SuccessMeter
        @SuppressWarnings(['EmptyMethod'])
        void successMethod() {
        }

        @SuccessMeter(baseName = "Pavement2", successSuffix = "ItIsGood")
        @SuppressWarnings(['EmptyMethod'])
        void anotherSuccessMethod() {
        }

        @SuccessMeter(baseName = "Pavement", failedSuffix = "IstRad")
        @SuppressWarnings(['EmptyMethod'])
        void failedMethod() {
            throw new RuntimeException()
        }

        @SuppressWarnings(['EmptyMethod'])
        void noAnnotationMethod() {
        }
    }

    private class TestModule implements Module {

        MetricRegistry registry

        TestModule(MetricRegistry registry) {
            this.registry = registry
        }

        @Override
        void configure(Binder binder) {
            MethodInvocationSuccessMeterInterceptor interceptor = new MethodInvocationSuccessMeterInterceptor()
            interceptor.setRegistry(this.registry)
            binder.bindInterceptor(any(), annotatedWith(SuccessMeter), interceptor)

            binder.bind(SuccessMeterTestClass)
        }
    }


}
