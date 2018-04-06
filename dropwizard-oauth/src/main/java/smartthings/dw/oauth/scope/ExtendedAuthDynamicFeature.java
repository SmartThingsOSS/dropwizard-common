package smartthings.dw.oauth.scope;

import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import jersey.repackaged.com.google.common.collect.ImmutableList;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.Annotation;
import java.util.List;

/**
 * A {@link DynamicFeature} that registers the provided auth filter
 * to resource methods annotated with the {@link RolesAllowed}, {@link PermitAll}
 * {@link DenyAll}, and {@link ScopesAllowed} annotations.
 *
 * <p>In conjunction with {@link RolesAllowedDynamicFeature} and {@link ScopesAllowedDynamicFeature}
 * it enables authorization <i>AND</i> authentication of requests on the annotated methods and classes.</p>
 *
 * <p>This class is based on {@link AuthDynamicFeature} but extended to support {@link ScopesAllowed} and
 * class level
 * annotations.</p>
 */
public class ExtendedAuthDynamicFeature implements DynamicFeature {
	private final ContainerRequestFilter authFilter;

	public ExtendedAuthDynamicFeature(ContainerRequestFilter authFilter) {
		this.authFilter = authFilter;
	}

	private static List<Class<? extends Annotation>> AUTH_ANNOTATIONS = ImmutableList.of(
			RolesAllowed.class, DenyAll.class, PermitAll.class, ScopesAllowed.class,
        FineGrainedScopeAllowed.class, FineGrainedScopesAllowed.class);

	private boolean hasMethodAnnotation(ResourceInfo resourceInfo) {
		AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
		for (Class<? extends Annotation> ac : getAuthAnnotations()) {
			if (am.isAnnotationPresent(ac)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasClassAnnotation(ResourceInfo resourceInfo) {
		Annotation[] annotations = resourceInfo.getResourceClass().getAnnotations();
		for (Annotation a : annotations) {
			if (getAuthAnnotations().contains(a.annotationType())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());
		resourceInfo.getResourceClass().getAnnotations();
		Annotation[][] parameterAnnotations = am.getParameterAnnotations();

		if (hasMethodAnnotation(resourceInfo) || hasClassAnnotation(resourceInfo)) {
			context.register(authFilter);
		} else {
			for (Annotation[] annotations : parameterAnnotations) {
				for (Annotation annotation : annotations) {
					if (annotation instanceof Auth) {
						context.register(authFilter);
						return;
					}
				}
			}
		}
	}

	/**
	 * Allows reuse of this class if all they want to do is change the annotations
	 * that are supported.
	 */
	protected List<Class<? extends Annotation>> getAuthAnnotations() {
		return AUTH_ANNOTATIONS;
	}
}
