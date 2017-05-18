package smartthings.dw.logging.turbo;

import ch.qos.logback.classic.turbo.TurboFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.turbo.TurboFilterFactory;

@JsonTypeName("sampling")
public class SamplingTurboFilterFactory implements TurboFilterFactory {

    @JsonProperty
    private double sampleRate = 1;


    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    @Override
    public TurboFilter build() {
        final SamplingTurboFilter turboFilter = new SamplingTurboFilter();
        turboFilter.setSampleRate(sampleRate);
        turboFilter.start();
        return turboFilter;
    }
}
