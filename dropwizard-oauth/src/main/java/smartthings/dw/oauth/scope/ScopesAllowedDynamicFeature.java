package smartthings.dw.oauth.scope;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Note that this code is based on {@link RolesAllowedDynamicFeature} but currently
 * does not intermix intelligently with those annotations. If a method or class uses
 * these annotations as well as the roll annotations both will trigger. Note that a
 * scope annotation on a method will override the annotation on a class.
 *
 * Filtering by scope can either be allowed by matching one of {@link ScopesAllowed} or
 * {@link FineGrainedScopeAllowed}.
 *
 * {@link ScopesAllowed} provides matching against an exact string which must be available within
 * the OAuth scopes.
 *
 * {@link FineGrainedScopeAllowed} provides dynamic matching using fine grained scopes format which can
 * depend on values that are resolved through the request's context parameters.
 */
public class ScopesAllowedDynamicFeature implements DynamicFeature {
    // this pattern matches only camel case starting with lower case, so sorry if I am forcing
    // you to conform, but this makes sense since they are for variables
    private static final Pattern varNamePattern = Pattern.compile("\\{([a-z]\\w*)\\}");

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext configuration) {
		AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

		// ScopesAllowed on the method takes precedence over class level annotations
		ScopesAllowed ra = am.getAnnotation(ScopesAllowed.class);
		FineGrainedScopeAllowed[] fgra = am.getAnnotationsByType(FineGrainedScopeAllowed.class);

		if (ra == null) {
            ra = resourceInfo.getResourceClass().getAnnotation(ScopesAllowed.class);
        }

        if (fgra.length == 0) {
            fgra = resourceInfo.getResourceClass().getAnnotationsByType(FineGrainedScopeAllowed.class);
        }

		if ((ra != null) || (fgra.length > 0)) {
			configuration.register(new ScopesAllowedRequestFilter(ra, fgra));
		}
	}

	@Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
	private static class ScopesAllowedRequestFilter implements ContainerRequestFilter {

		private final String[] scopesAllowed;
		private final FineGrainedScopeAllowed[] fineGrainedScopesAllowed;

		ScopesAllowedRequestFilter(final ScopesAllowed scopesAllowed,
                                   final FineGrainedScopeAllowed[] fineGrainedScopesAllowed) {
			if ((scopesAllowed != null) && (scopesAllowed.value() != null)) {
			    this.scopesAllowed = scopesAllowed.value();
            } else {
                this.scopesAllowed = new String[]{};
            }

            if ((fineGrainedScopesAllowed != null)) {
			    this.fineGrainedScopesAllowed = fineGrainedScopesAllowed;
            }
            else {
			    this.fineGrainedScopesAllowed = new FineGrainedScopeAllowed[]{};
            }

            verifyFineGrainedScopes();
		}

		@Override
		public void filter(final ContainerRequestContext requestContext) throws IOException {
		    if (isAuthenticated(requestContext)) {
                Principal principal = requestContext.getSecurityContext().getUserPrincipal();
                if (principal instanceof ScopeSupport) {
                    final Collection<String> scopes = Collections.unmodifiableCollection(
                        ((ScopeSupport) principal).getScopes());

                    for (String allowedScope : scopesAllowed) {
                        if (scopes.contains(allowedScope)) {
                            return;
                        }
                    }

                    for (FineGrainedScopeAllowed allowedScope : fineGrainedScopesAllowed) {
                        if (checkFineGrainedScope(scopes, allowedScope, requestContext)) {
                            return;
                        }
                    }
                }
            }

			throw new ForbiddenException(LocalizationMessages.USER_NOT_AUTHORIZED());
		}

		private void verifyFineGrainedScopes() {
		    for (FineGrainedScopeAllowed scope : fineGrainedScopesAllowed) {
                Set<String> varNames = Arrays.stream(scope.varInfo()).map(x -> x.name()).collect(Collectors.toSet());
                Set<String> patternVars = new HashSet<>();

                Matcher m = varNamePattern.matcher(scope.scope());
                while (m.find()) {
                    // get match inside {}
                    patternVars.add(m.group(1));
                }

                Set<String> intersection = new HashSet<>(varNames);
                intersection.retainAll(patternVars);

                if ((varNames.size() != patternVars.size()) || (intersection.size() != patternVars.size())) {
                    throw new IllegalArgumentException("List variables defined in scope pattern do not match variable info");
                }
            }
        }

        private static boolean checkFineGrainedScope(Collection<String> scopes, FineGrainedScopeAllowed scopeAllowed,
                                              ContainerRequestContext context) {
		    String contextAwareScope = renderContextFineGrainedScope(scopeAllowed, context);

		    if (contextAwareScope != null) {
                return fineGrainedScopePartsMatch(scopes, contextAwareScope);
            }

            return false;
        }

        private static String renderContextFineGrainedScope(FineGrainedScopeAllowed scopeAllowed, ContainerRequestContext context) {
            String contextAwareScope = scopeAllowed.scope();
            for (VarInfo info : scopeAllowed.varInfo()) {
                String pattern = String.format("{%s}", info.name());

                MultivaluedMap<String, String> contextParams;
                switch (info.type()) {
                    case PATH:
                        contextParams = context.getUriInfo().getPathParameters();
                        break;
                    case QUERY:
                        contextParams = context.getUriInfo().getQueryParameters();
                        break;
                    case HEADER:
                        contextParams = context.getHeaders();
                        break;
                    default:
                        contextParams = null;
                }

                String value = contextParams.getFirst(info.name());

                if (value == null) {
                    return null;
                }

                contextAwareScope = contextAwareScope.replace(pattern, value);
            }

            return contextAwareScope;
        }

        private static boolean fineGrainedScopePartsMatch(Collection<String> scopes, String allowedScope) {
		    String[] allowedScopedParts = allowedScope.split(":");
		    for (String scope : scopes) {
                String[] scopeParts = scope.split(":");
		        boolean matches = true;

		        if (scopeParts.length == allowedScopedParts.length) {
                    for (int i = 0; i < allowedScopedParts.length; i++) {
                        if (scopeParts[i].equals("*") || allowedScopedParts[i].equals("*") || allowedScopedParts[i].equals(scopeParts[i])) {
                            matches = matches && true;
                        } else {
                            matches = false;
                        }
                    }
                } else {
		            matches = false;
                }

                if (matches) {
		            return true;
                }
            }

            return false;
        }

		private static boolean isAuthenticated(final ContainerRequestContext requestContext) {
			return requestContext.getSecurityContext().getUserPrincipal() != null;
		}
	}
}
