package smartthings.dw.logging;

import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.UUID;

public class LoggingFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing to do
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		String requestId = null;
		if (request instanceof HttpServletRequest) {
			HttpServletRequestWrapper req = new HttpServletRequestWrapper((HttpServletRequest) request);
			requestId = req.getHeader(LoggingContext.CORRELATION_ID_HEADER);
		}
		if (requestId == null || requestId.isEmpty()) {
			requestId = UUID.randomUUID().toString();
		}
		MDC.put(LoggingContext.LOGGING_ID, requestId);
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(LoggingContext.LOGGING_ID);
		}
	}

	@Override
	public void destroy() {
		// nothing to do
	}
}
