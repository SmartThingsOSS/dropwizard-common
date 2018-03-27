package smartthings.dw.oauth;

import javax.ws.rs.WebApplicationException;

public class SlowResponseException extends WebApplicationException {
    public SlowResponseException() {
        super(520);
    }
}
