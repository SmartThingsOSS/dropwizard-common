package smartthings.dw.oauth.scope;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Allows to specify allowed scopes for resource classes or methods that follow the fine grained scope format and
 * also can dynamically match the scope against a request's context parameters.
 *
 * To specify a scope that should match dynamically against the context parameters the scope must be written similar
 * to DropWizard's URI template parameters, where the name of the context variable should be in be between curly braces.
 *
 * varInfo must be specified, which will indicate the name of the variable and where in the context can it be obtained
 * from (e.g. path, query or headers).
 */
@Repeatable(FineGrainedScopesAllowed.class)
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface FineGrainedScopeAllowed {
    String scope();
    VarInfo[] varInfo();
}
