package smartthings.dw.cassandra.addresstranslater;

import com.datastax.driver.core.policies.AddressTranslater;
import com.datastax.driver.core.policies.EC2MultiRegionAddressTranslater;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("ec2MultiRegion")
public class EC2MultiRegionAddressTranslaterFactory implements AddressTranslaterFactory {
	private final static Logger LOG = LoggerFactory.getLogger(EC2MultiRegionAddressTranslaterFactory.class);

	@Override
	public AddressTranslater build() {
		LOG.info("EC2MultiRegionAddressTranslaterFactory");
		return new EC2MultiRegionAddressTranslater();
	}
}
