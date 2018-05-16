package smartthings.dw.zipkin;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface SpanReporterFactory extends Discoverable {

    Reporter<Span> build();
}
