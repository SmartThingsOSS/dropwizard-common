package smartthings.dw.oauth;

import io.dropwizard.auth.Authorizer;

public class TokenAuthorizer implements Authorizer<OAuthToken> {
	@Override
	public boolean authorize(OAuthToken token, String role) {
		return token.getUser().isPresent() && token.getUser().get().getAuthorities().contains(role);
	}
}
