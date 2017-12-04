package smartthings.dw.cassandra.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that helps to prune the cql statement generated from PreparedStatement.
 * This will help eliminate tombstone creations in the cluster
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PruneCql {
}
