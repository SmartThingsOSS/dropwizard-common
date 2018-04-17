package smartthings.dw.oauth.exceptions;

import javax.ws.rs.WebApplicationException;

public class ConnectionException extends WebApplicationException {
	public ConnectionException() {
		super(522);
	}
}
