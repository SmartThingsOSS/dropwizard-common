package smartthings.dw.zipkin;

import com.fasterxml.jackson.annotation.JsonTypeName;
import zipkin.Span;
import zipkin.reporter.Reporter;

@JsonTypeName("empty")
public class EmptySpanReporterFactory implements SpanReporterFactory {
    @Override
    public Reporter<Span> build() {
        return Reporter.NOOP;
    }
}
