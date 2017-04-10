package smartthings.dw.logging;

import org.slf4j.MDC;
import org.slf4j.event.Level;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class LoggingFilter implements Filter {

    private static final Level[] allLevels = {
        Level.ERROR,
        Level.WARN,
        Level.INFO,
        Level.DEBUG,
        Level.TRACE
    };

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
            Optional.ofNullable(req.getHeader(LoggingContext.LOG_LEVEL_HEADER))
                .map(String::toUpperCase)
                .filter(logLevelStr -> Arrays.stream(allLevels).anyMatch(level -> level.toString().equals(logLevelStr)))
                .ifPresent(logLevel -> MDC.put(LoggingContext.DYNAMIC_LOG_LEVEL, logLevel));
        }
		if (requestId == null || requestId.isEmpty()) {
			requestId = UUID.randomUUID().toString();
		}
		MDC.put(LoggingContext.LOGGING_ID, requestId);
		try {
			chain.doFilter(request, response);
		} finally {
			MDC.remove(LoggingContext.LOGGING_ID);
			MDC.remove(LoggingContext.DYNAMIC_LOG_LEVEL);
		}
	}

	@Override
	public void destroy() {
		// nothing to do
	}
}
