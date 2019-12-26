package io.dropwizard.jersey.protobuf;

import com.google.protobuf.Message;

import io.dropwizard.jersey.protobuf.ProtocolBufferMessageBodyProvider;
import io.dropwizard.jersey.protobuf.protos.DropwizardProtosTest.Example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedHashMap;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ProtocolBufferMessageBodyProviderTest {
    private static final Annotation[] NONE = new Annotation[0];
    private final ProtocolBufferMessageBodyProvider provider
                                  = new ProtocolBufferMessageBodyProvider();

    public static Example example = Example.newBuilder().setId(1337L).build();

    @Test
    public void readsDeserializableTypes() throws Exception {
        assertThat(provider.isReadable(Example.class, null, null, null))
                .isTrue();
    }

    @Test
    public void writesSerializableTypes() throws Exception {
        assertThat(provider.isWriteable(Example.class, null, null, null))
                .isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void deserializesRequestEntities() throws Exception {
        final ByteArrayInputStream entity = new ByteArrayInputStream(example.toByteArray());
        final Class<?> klass = Example.class;

        final Object obj = provider.readFrom((Class<Message>) klass,
                                             Example.class,
                                             NONE,
                                             ProtocolBufferMediaType.APPLICATION_PROTOBUF_TYPE,
                                             new MultivaluedHashMap<String,String>(),
                                             entity);

        assertThat(obj)
                .isInstanceOf(Example.class);

        assertThat(((Example) obj).getId())
                .isEqualTo(1337L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void throwsAWebApplicationExceptionForMalformedRequestEntities() throws Exception {
        final ByteArrayInputStream entity = new ByteArrayInputStream("{\"id\":-1d".getBytes());

        try {
            final Class<?> klass = Example.class;
            provider.readFrom((Class<Message>) klass,
                              Example.class,
                              NONE,
                              ProtocolBufferMediaType.APPLICATION_PROTOBUF_TYPE,
                              new MultivaluedHashMap<String,String>(),
                              entity);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getMessage())
                    .startsWith("HTTP 500 Internal Server Error");
        }
    }

    @Test
    public void serializesResponseEntities() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final Example example = Example.newBuilder().setId(1L).build();

        provider.writeTo(example,
                         Example.class,
                         Example.class,
                         NONE,
                         ProtocolBufferMediaType.APPLICATION_PROTOBUF_TYPE,
                         new StringKeyIgnoreCaseMultivaluedMap<>(),
                         output);

        assertThat(output.toByteArray())
                .isEqualTo(example.toByteArray());
    }

    @Test
    public void responseEntitySize() throws Exception {
        final Example example = Example.newBuilder().setId(1L).build();

        final long size = provider.getSize(example,
                                           Example.class,
                                           Example.class,
                                           NONE,
                                           ProtocolBufferMediaType.APPLICATION_PROTOBUF_TYPE);

        assertThat(size).isEqualTo(2L);
    }
}
