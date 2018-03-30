package smartthings.dw.oauth.exceptions;

import javax.ws.rs.WebApplicationException;

public class TimeoutException extends WebApplicationException {
	public TimeoutException() {
		super(524);
	}
}
