package smartthings.dw.oauth;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class OrderedPredicateAuthenticator implements OAuthAuthenticator {

    private final List<PredicateAuthenticatorTuple> predicateAuthenticatorTuples;

    public OrderedPredicateAuthenticator(List<PredicateAuthenticatorTuple> tuples) {
        if (tuples == null || tuples.isEmpty()) {
            throw new IllegalArgumentException("Authenticator list must not be empty.");
        }
        this.predicateAuthenticatorTuples = ImmutableList.copyOf(tuples);
    }

    @Override
    public Optional<OAuthToken> authenticate(String credentials) throws AuthenticationException {
        for (PredicateAuthenticatorTuple predicateValidator : predicateAuthenticatorTuples) {
            if (predicateValidator.canAuthenticate(credentials)) {
                return predicateValidator.getAuthenticator().authenticate(credentials);
            }
        }
        return Optional.empty();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(List<PredicateAuthenticatorTuple> predicateAuthenticatorTuples) {
        return new Builder(predicateAuthenticatorTuples);
    }

    public static class Builder {
        private List<PredicateAuthenticatorTuple> predicateAuthenticatorTuples = new ArrayList<>();

        public Builder() {

        }

        public Builder(List<PredicateAuthenticatorTuple> predicateAuthenticatorTuples) {
            this.predicateAuthenticatorTuples = predicateAuthenticatorTuples;
        }

        public List<PredicateAuthenticatorTuple> getPredicateAuthenticatorTuples() {
            return predicateAuthenticatorTuples;
        }

        public Builder setPredicateAuthenticatorTuples(List<PredicateAuthenticatorTuple> predicateAuthenticatorTuples) {
            this.predicateAuthenticatorTuples.addAll(predicateAuthenticatorTuples);
            return this;
        }

        public Builder addPredicateAuthenticatorTuple(PredicateAuthenticatorTuple tuple) {
            this.predicateAuthenticatorTuples.add(tuple);
            return this;
        }

        public Builder addPredicateAuthenticatorTuple(
            Predicate<String> predicate,
            Authenticator<String, OAuthToken> authenticator
        ) {
            this.predicateAuthenticatorTuples.add(new PredicateAuthenticatorTuple(predicate, authenticator));
            return this;
        }

        public OrderedPredicateAuthenticator build() {
            return new OrderedPredicateAuthenticator(predicateAuthenticatorTuples);
        }
    }
}
