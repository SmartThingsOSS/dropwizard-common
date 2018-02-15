package smartthings.dw.cassandra;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = {CassandraConfigurationValidator.class})
public @interface ValidCassandraConfiguration {
    String message() default "CassandraConfiguration fails ValidCassandraConfiguration constraint";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
