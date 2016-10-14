package smartthings.dw.asynchttpclient;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;
import org.asynchttpclient.filter.FilterContext;
import org.asynchttpclient.filter.FilterException;
import org.asynchttpclient.filter.RequestFilter;
import org.slf4j.MDC;
import smartthings.dw.logging.LoggingContext;

import java.util.Map;

public class CorrelationIdFilter implements RequestFilter {

	@Override
	public <T> FilterContext<T> filter(FilterContext<T> ctx) throws FilterException {

		final Map<String, String> mdc = MDC.getCopyOfContextMap();
		final AsyncHandler<T> asyncHandler = ctx.getAsyncHandler();

		// Wrap AsyncHandler to copy MDC since it executes on a different thread
		FilterContext<T> newContext = new FilterContext.FilterContextBuilder<>(ctx)
				.asyncHandler(new AsyncHandler<T>() {
					@Override
					public void onThrowable(Throwable t) {
						try {
							mdc.forEach(MDC::put);
							asyncHandler.onThrowable(t);
						} finally {
							MDC.clear();
						}
					}

					@Override
					public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
						State result;
						try {
							mdc.forEach(MDC::put);
							result = asyncHandler.onBodyPartReceived(bodyPart);
						} finally {
							MDC.clear();
						}
						return result;
					}

					@Override
					public State onStatusReceived(HttpResponseStatus responseStatus) throws Exception {
						try {
							mdc.forEach(MDC::put);
							return asyncHandler.onStatusReceived(responseStatus);
						} finally {
							MDC.clear();
						}
					}

					@Override
					public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
						try {
							mdc.forEach(MDC::put);
							return asyncHandler.onHeadersReceived(headers);
						} finally {
							MDC.clear();
						}
					}

					@Override
					public T onCompleted() throws Exception {
						try {
							mdc.forEach(MDC::put);
							return asyncHandler.onCompleted();
						} finally {
							MDC.clear();
						}
					}
				}).build();

		String loggingId = LoggingContext.getLoggingId();

		if (loggingId != null) {
			HttpHeaders headers = newContext.getRequest().getHeaders();
			if (!headers.contains(LoggingContext.CORRELATION_ID_HEADER)) {
				headers.add(LoggingContext.CORRELATION_ID_HEADER, loggingId);
			}
		}
		return newContext;
	}
}
