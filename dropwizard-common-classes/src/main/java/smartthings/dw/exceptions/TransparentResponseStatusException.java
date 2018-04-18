package smartthings.dw.exceptions;

import javax.ws.rs.WebApplicationException;

public class TransparentResponseStatusException extends WebApplicationException {
    public TransparentResponseStatusException(final int status) {
        super(status);
    }

    public int getStatus() {
        return getResponse().getStatus();
    }
}
