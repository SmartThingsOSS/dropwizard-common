package smartthings.dw.cassandra.addresstranslater;

import com.datastax.driver.core.policies.AddressTranslater;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AddressTranslaterFactory extends Discoverable {
	AddressTranslater build();
}
