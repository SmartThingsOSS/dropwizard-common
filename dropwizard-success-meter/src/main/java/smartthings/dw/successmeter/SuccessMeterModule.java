package smartthings.dw.successmeter;


import smartthings.dw.guice.AbstractDwModule;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

public class SuccessMeterModule extends AbstractDwModule {

    @Override
    protected void configure() {
        MethodInvocationSuccessMeterInterceptor interceptor = new MethodInvocationSuccessMeterInterceptor();
        requestInjection(interceptor);
        bindInterceptor(any(), annotatedWith(SuccessMeter.class), interceptor);
    }
}
