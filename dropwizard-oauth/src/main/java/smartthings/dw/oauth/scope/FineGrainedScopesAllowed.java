package smartthings.dw.oauth.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container annotation for multiple @FineGrainedScopeAllow. It is implicitly used in Java 8, thanks the @Repeatable
 * annotation in {@link FineGrainedScopesAllowed}. When using Groovy the container needs to explicitly contains all
 * the {@link FineGrainedScopeAllowed} entries.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface FineGrainedScopesAllowed {
    FineGrainedScopeAllowed[] value();
}
