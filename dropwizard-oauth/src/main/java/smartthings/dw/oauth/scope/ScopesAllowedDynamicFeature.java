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
import java.io.IOException;
import java.security.Principal;

/**
 * Note that this code is based on {@link RolesAllowedDynamicFeature} but currently
 * does not intermix intelligently with those annotations. If a method or class uses
 * these annotations as well as the roll annotations both will trigger. Note that a
 * scope annotation on a method will override the annotation on a class.
 */
public class ScopesAllowedDynamicFeature implements DynamicFeature {

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext configuration) {
		AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

		// ScopesAllowed on the method takes precedence over class level annotations
		ScopesAllowed ra = am.getAnnotation(ScopesAllowed.class);
		if (ra != null) {
			configuration.register(new ScopesAllowedRequestFilter(ra.value()));
			return;
		}

		ra = resourceInfo.getResourceClass().getAnnotation(ScopesAllowed.class);
		if (ra != null) {
			configuration.register(new ScopesAllowedRequestFilter(ra.value()));
		}
	}

	@Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
	private static class ScopesAllowedRequestFilter implements ContainerRequestFilter {

		private final String[] scopesAllowed;

		ScopesAllowedRequestFilter(final String[] scopesAllowed) {
			this.scopesAllowed = (scopesAllowed != null) ? scopesAllowed : new String[]{};
		}

		@Override
		public void filter(final ContainerRequestContext requestContext) throws IOException {
			if (scopesAllowed.length > 0 && !isAuthenticated(requestContext)) {
				throw new ForbiddenException(LocalizationMessages.USER_NOT_AUTHORIZED());
			}

			Principal principal = requestContext.getSecurityContext().getUserPrincipal();
			if (principal instanceof ScopeSupport) {
				for (String role : scopesAllowed) {
					if (((ScopeSupport) principal).getScopes().contains(role)) {
						return;
					}
				}
			}
			throw new ForbiddenException(LocalizationMessages.USER_NOT_AUTHORIZED());
		}

		private static boolean isAuthenticated(final ContainerRequestContext requestContext) {
			return requestContext.getSecurityContext().getUserPrincipal() != null;
		}
	}
}
