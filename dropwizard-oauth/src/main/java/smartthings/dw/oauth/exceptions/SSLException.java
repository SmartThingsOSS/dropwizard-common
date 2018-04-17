package smartthings.dw.oauth.exceptions;

import javax.ws.rs.WebApplicationException;

public class SSLException extends WebApplicationException {
	public SSLException() {
		super(525);
	}
}
