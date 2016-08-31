package smartthings.dw.oauth.scope;

import java.security.Principal;
import java.util.Collection;

/**
 * This annotation should be added to {@link Principal} classes that would like to
 * support authenticating using the {@link @ScopesAllowed} annotation via
 * {@link ScopesAllowedDynamicFeature}.
 */
public interface ScopeSupport {
	Collection<String> getScopes();
}
