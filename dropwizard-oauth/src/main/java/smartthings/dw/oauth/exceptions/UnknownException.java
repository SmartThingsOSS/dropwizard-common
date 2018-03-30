package smartthings.dw.oauth.exceptions;

import javax.ws.rs.WebApplicationException;

public class UnknownException extends WebApplicationException {
	public UnknownException() {
		super(520);
	}
}
