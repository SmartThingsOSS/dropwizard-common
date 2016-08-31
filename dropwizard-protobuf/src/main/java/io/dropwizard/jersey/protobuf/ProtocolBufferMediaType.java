package io.dropwizard.jersey.protobuf;
import javax.ws.rs.core.MediaType;
/**
 * Copied from https://github.com/dropwizard/dropwizard-protobuf which is not yet compatible with protocol buffer 3.0
 * Licence: https://raw.githubusercontent.com/dropwizard/dropwizard-protobuf/master/LICENSE
 */
public class ProtocolBufferMediaType extends MediaType {
    public final static String APPLICATION_PROTOBUF = "application/x-protobuf";
    public final static MediaType APPLICATION_PROTOBUF_TYPE = new MediaType("application","x-protobuf");
}
