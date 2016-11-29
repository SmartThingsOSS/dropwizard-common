package smartthings.dw.timer;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

public class MethodInvocationTimingInterceptor implements MethodInterceptor {

    private MetricRegistry registry;

    @Inject
    public void setRegistry(MetricRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Timed timedAnnotation = invocation.getMethod().getAnnotation(Timed.class);
        String name = findName(timedAnnotation.name(), timedAnnotation.absolute(), invocation.getMethod());

        Timer.Context ctx = registry.timer(name).time();

        try {
            return invocation.proceed();
        } finally {
            ctx.stop();
        }
    }

    private String findName(String name, boolean absolute, Method method) {
        if (name == null || name.isEmpty()) {
            return name(method.getDeclaringClass(), method.getName());
        }
        if (absolute) {
            return name;
        }
        return name(method.getDeclaringClass(), name);
    }
}
