package smartthings.dw.cassandra

import io.dropwizard.configuration.ConfigurationValidationException
import io.dropwizard.configuration.YamlConfigurationFactory
import io.dropwizard.jackson.Jackson
import io.dropwizard.validation.BaseValidator
import spock.lang.Specification

import javax.validation.Validator


class CassandraConfigurationSpec extends Specification {
    static final Validator validator = BaseValidator.newValidator();
    static final YamlConfigurationFactory factory = new YamlConfigurationFactory<>(
        CassandraConfiguration.class, validator, Jackson.newObjectMapper(), "dw");

    def 'configuration is deserialized if custom validtion query does specify idempotence'() {
        given:
        File f = new File(this.class.classLoader.getResource('valid-cassandra-config.yml').toURI())

        when:
        factory.build(f)

        then:
        noExceptionThrown()
    }

    def 'configuration fails validation if custom validtion query does not specify idempotence'() {
        given:
        File f = new File(this.class.classLoader.getResource('invalid-cassandra-config.yml').toURI())

        when:
        factory.build(f)

        then:
        thrown(ConfigurationValidationException)
    }
}
