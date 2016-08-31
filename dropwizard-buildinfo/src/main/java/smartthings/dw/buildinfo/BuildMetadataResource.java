package smartthings.dw.buildinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartthings.dw.guice.WebResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Path("/buildinfo")
@Produces(MediaType.APPLICATION_JSON)
public class BuildMetadataResource implements WebResource {
	private static final Logger LOG = LoggerFactory.getLogger(BuildMetadataResource.class);
	private static final String METADATA_LOCATION = "/build.properties";
	private static final MetadataResponse METADATA = loadProperties(METADATA_LOCATION);

	@GET
	public MetadataResponse getInfo() {
		return METADATA;
	}

	static protected MetadataResponse loadProperties(String metadataLocation) {
		try {
			InputStream stream = BuildMetadataResource.class.getResourceAsStream(metadataLocation);
			if (stream != null) {
				Properties properties = new Properties();
				properties.load(stream);
				stream.close();
				return new ObjectMapper().convertValue(Maps.fromProperties(properties), MetadataResponse.class);
			} else {
				LOG.info("Metadata with build information was not found");
			}
		} catch (IOException e) {
			LOG.warn("Error loading meta data properties", e);
		}
		return new MetadataResponse();
	}

}
