package smartthings.dw.cassandra;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CassandraConfigurationValidator implements ConstraintValidator<ValidCassandraConfiguration, CassandraConfiguration> {
    @Override
    public void initialize(ValidCassandraConfiguration annotation) {
    }

    @Override
    public boolean isValid(CassandraConfiguration config, ConstraintValidatorContext context) {
        boolean valid = true;
        if (!config.DEFAULT_VALIDATION_QUERY.equals(config.getValidationQuery())) {
            if (config.validationQueryIdempotence == null) {
                context.buildConstraintViolationWithTemplate("must be defined "+
                    "when validationQuery is defined ")
                    .addPropertyNode("validationQueryIdempotence")
                    .addBeanNode()
                    .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}
