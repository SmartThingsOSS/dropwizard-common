package smartthings.dw.cassandra.addresstranslator;

import com.datastax.driver.core.policies.AddressTranslator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AddressTranslatorFactory extends Discoverable {
    AddressTranslator build();
}
