package smartthings.dw.oauth;

import io.dropwizard.auth.Authenticator;

public interface OAuthAuthenticator extends Authenticator<String, OAuthToken> {
}
