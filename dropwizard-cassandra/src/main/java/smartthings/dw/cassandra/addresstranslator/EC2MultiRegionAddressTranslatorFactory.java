package smartthings.dw.cassandra.addresstranslator;

import com.datastax.driver.core.policies.AddressTranslator;
import com.datastax.driver.core.policies.EC2MultiRegionAddressTranslator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("ec2MultiRegion")
public class EC2MultiRegionAddressTranslatorFactory implements AddressTranslatorFactory {
	private final static Logger LOG = LoggerFactory.getLogger(EC2MultiRegionAddressTranslatorFactory.class);

	@Override
	public AddressTranslator build() {
		LOG.info("EC2MultiRegionAddressTranslaterFactory");
		return new EC2MultiRegionAddressTranslator();
	}
}
