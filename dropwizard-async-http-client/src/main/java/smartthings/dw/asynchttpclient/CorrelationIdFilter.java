package smartthings.dw.asynchttpclient;

import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.RequestFilter;
import smartthings.dw.logging.LoggingContext;

public class CorrelationIdFilter implements RequestFilter {

	@Override
	public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {
		String loggingId = LoggingContext.getLoggingId();
		if (loggingId != null) {
			ctx.getRequest().getHeaders().add(LoggingContext.CORRELATION_ID_HEADER, loggingId);
		}
		return ctx;
	}
}
