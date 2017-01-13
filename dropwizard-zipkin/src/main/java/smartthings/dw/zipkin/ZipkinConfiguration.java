package smartthings.dw.zipkin;

import javax.validation.constraints.NotNull;

public class ZipkinConfiguration {

    private SpanReporterFactory reporter;

    @NotNull
    private String serviceName;

    private String serviceHost = "127.0.0.1";

    private int servicePort = 8080;

    private float sampleRate = 1.0f;

    private boolean traceId128Bit = false;

    public SpanReporterFactory getReporter() {
        return reporter;
    }

    public void setReporter(SpanReporterFactory reporter) {
        this.reporter = reporter;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceHost() {
        return serviceHost;
    }

    public void setServiceHost(String serviceHost) {
        this.serviceHost = serviceHost;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public float getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(float sampleRate) {
        this.sampleRate = sampleRate;
    }

    public boolean isTraceId128Bit() {
        return traceId128Bit;
    }

    public void setTraceId128Bit(boolean traceId128Bit) {
        this.traceId128Bit = traceId128Bit;
    }
}
