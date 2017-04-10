package smartthings.dw.logging;

import org.slf4j.MDC;

public class LoggingContext {
    public final static String CORRELATION_ID_HEADER = "X-ST-CORRELATION";
    public final static String LOG_LEVEL_HEADER = "X-ST-LOG-LEVEL";
    public final static String LOGGING_ID = "loggingId";
    public final static String DYNAMIC_LOG_LEVEL = "dynamicLogLevel";

    private LoggingContext() {
        // Static only
    }

    public static String getLoggingId() {
        return MDC.get(LOGGING_ID);
    }

    public static String getLogLevel() {
        return MDC.get(DYNAMIC_LOG_LEVEL);
    }
}
