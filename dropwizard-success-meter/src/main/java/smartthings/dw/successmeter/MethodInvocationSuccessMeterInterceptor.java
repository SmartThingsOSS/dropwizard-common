package smartthings.dw.successmeter;


import com.codahale.metrics.MetricRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;
import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

public class MethodInvocationSuccessMeterInterceptor implements MethodInterceptor {

    private MetricRegistry registry;

    @Inject
    public void setRegistry(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (registry == null) {
            throw new IllegalStateException("Registry must be set");
        }
        SuccessMeter annotation = invocation.getMethod().getAnnotation(SuccessMeter.class);
        String successName = findName(annotation.baseName(), annotation.successSuffix(), invocation.getMethod());
        String failedName = findName(annotation.baseName(), annotation.failedSuffix(), invocation.getMethod());

        try {
            Object returnValue = invocation.proceed();
            registry.meter(successName).mark();
            return returnValue;
        } catch (Exception e) {
            registry.meter(failedName).mark();
            throw e;
        }
    }

    private String findName(String name, String suffix, Method method) {
        if (name == null || name.isEmpty()) {
            return name(method.getDeclaringClass(), method.getName(), suffix);
        } else {
            return name(name, suffix);
        }
    }
}
