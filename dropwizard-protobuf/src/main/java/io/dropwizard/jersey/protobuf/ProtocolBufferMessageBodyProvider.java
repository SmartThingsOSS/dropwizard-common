package io.dropwizard.jersey.protobuf;

import com.google.protobuf.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * A Jersey provider which enables using Protocol Buffers to parse request
 * entities into objects and generate response entities from objects.
 *
 * Copied from https://github.com/dropwizard/dropwizard-protobuf which is not yet compatible with protocol buffer 3.0
 * This class contains a small change in order to work with protocol buffer 3.0, which is noted below.
 * Licence: https://raw.githubusercontent.com/dropwizard/dropwizard-protobuf/master/LICENSE
 *
 */
@Provider
@Consumes(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
@Produces(ProtocolBufferMediaType.APPLICATION_PROTOBUF)
public class ProtocolBufferMessageBodyProvider implements MessageBodyWriter<Message>, MessageBodyReader<Message>{
	@Override
	public long getSize(final Message m, final Class<?> type,
			final Type genericType, final Annotation[] annotations,
			final MediaType mediaType) {
		return m.getSerializedSize();
	}

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType,
			final Annotation[] annotations, final MediaType mediaType) {
		return Message.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(final Message m, final Class<?> type,
			final Type genericType, final Annotation[] annotations,
			final MediaType mediaType,
			final MultivaluedMap<String, Object> httpHeaders,
			final OutputStream entityStream) throws IOException {
		entityStream.write(m.toByteArray());
	}

	@Override
	public Message readFrom(
		final Class<Message> type,
		final Type genericType,
		final Annotation[] annotations,
		final MediaType mediaType,
		final MultivaluedMap<String, String> httpHeaders,
		final InputStream entityStream
	) throws IOException {

		try {
			Method newBuilder = type.getMethod("newBuilder");
			//This line was changed from the original
			Message.Builder builder = (Message.Builder) newBuilder.invoke(type);

			return builder.mergeFrom(entityStream).build();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		}
	}

	@Override
	public boolean isReadable(
			final Class<?> type,
			final Type genericType,
			final Annotation[] annotations,
			final MediaType mediaType) {
		return Message.class.isAssignableFrom(type);
	}
}
