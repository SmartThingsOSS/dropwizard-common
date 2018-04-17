package smartthings.dw.oauth.exceptions;

import javax.ws.rs.WebApplicationException;

public class CircuitBreakerOpenException extends WebApplicationException {
	public CircuitBreakerOpenException() {
		super(521);
	}
}
