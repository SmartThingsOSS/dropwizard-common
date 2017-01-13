package smartthings.dw.zipkin;

import com.fasterxml.jackson.annotation.JsonTypeName;
import zipkin.Span;
import zipkin.reporter.Reporter;

@JsonTypeName("console")
public class ConsoleSpanReporterFactory implements SpanReporterFactory {
    @Override
    public Reporter<Span> build() {
        return Reporter.CONSOLE;
    }
}
