package smartthings.dw.asynchttpclient;

public class TracingRequestFilterConfiguration {
    private Boolean startNewTraces = true;

    public Boolean getStartNewTraces() {
        return startNewTraces;
    }

    public void setStartNewTraces(Boolean startNewTraces) {
        this.startNewTraces = startNewTraces;
    }
}
