package smartthings.dw.logging.turbo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SamplingTurboFilter extends TurboFilter {

    public final static Marker SAMPLING_MARKER = MarkerFactory.getMarker("sampling");

    private final static int SAMPLE_RANGE = 10000;
    private final static double MINIMAL_SAMPLE_RATE = 1.0 / SAMPLE_RANGE;

    private long boundary = SAMPLE_RANGE;

    public void setSampleRate(Double sampleRate) {
        if (sampleRate != 0 && (sampleRate < MINIMAL_SAMPLE_RATE || sampleRate > 1)) {
            throw new IllegalArgumentException("Sample rate must be between " + MINIMAL_SAMPLE_RATE + " and 1");
        }
        this.boundary = (long) (sampleRate * SAMPLE_RANGE);
    }

    @Override
    public void start() {
            super.start();
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String s, Object[] objects, Throwable throwable) {
        if (!isStarted()) {
            return FilterReply.NEUTRAL;
        }
        if (SAMPLING_MARKER.equals(marker)) {
            if (ThreadLocalRandom.current().nextInt(SAMPLE_RANGE) <= boundary - 1) {
                return FilterReply.NEUTRAL;
            } else {
                return FilterReply.DENY;
            }
        } else {
            return FilterReply.NEUTRAL;
        }
    }
}
