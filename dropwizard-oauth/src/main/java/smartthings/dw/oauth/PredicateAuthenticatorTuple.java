package smartthings.dw.oauth;

import io.dropwizard.auth.Authenticator;

import java.util.function.Predicate;

public class PredicateAuthenticatorTuple {

    private final Predicate<String> predicate;
    private final Authenticator<String, OAuthToken> authenticator;

    public PredicateAuthenticatorTuple(Predicate<String> predicate, Authenticator<String, OAuthToken> authenticator) {
        if (predicate == null) {
            throw new IllegalArgumentException("Authenticator predicate must not be null.");
        }
        this.predicate = predicate;

        if (authenticator == null) {
            throw new IllegalArgumentException("Authenticator must not be null.");
        }
        this.authenticator = authenticator;
    }

    public Predicate<String> getPredicate() {
        return predicate;
    }

    public Authenticator<String, OAuthToken> getAuthenticator() {
        return authenticator;
    }

    public boolean canAuthenticate(String token) {
        return predicate.test(token);
    }
}
