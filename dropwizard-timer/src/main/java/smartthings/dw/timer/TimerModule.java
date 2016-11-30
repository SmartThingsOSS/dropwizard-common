package smartthings.dw.timer;

import com.codahale.metrics.annotation.Timed;
import smartthings.dw.guice.AbstractDwModule;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

public class TimerModule extends AbstractDwModule {

    @Override
    protected void configure() {
        MethodInvocationTimingInterceptor interceptor = new MethodInvocationTimingInterceptor();
        requestInjection(interceptor);
        bindInterceptor(any(), annotatedWith(Timed.class), interceptor);
    }
}
