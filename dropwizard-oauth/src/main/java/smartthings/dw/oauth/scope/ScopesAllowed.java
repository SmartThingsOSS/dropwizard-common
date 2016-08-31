package smartthings.dw.oauth.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to mark that resource classes or methods should have one of the listed scopes.
 * See also {@link ScopesAllowedDynamicFeature} and {@link ScopeSupport}.
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface ScopesAllowed {
	String[] value();
}

