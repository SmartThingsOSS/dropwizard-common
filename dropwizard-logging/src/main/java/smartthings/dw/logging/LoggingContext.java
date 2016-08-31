package smartthings.dw.logging;

import org.slf4j.MDC;

public class LoggingContext {
	public final static String CORRELATION_ID_HEADER = "X-ST-CORRELATION";
	protected final static String LOGGING_ID = "loggingId";

	private LoggingContext(){
		// Static only
	}

	public static String getLoggingId() {
		return MDC.get(LOGGING_ID);
	}
}
