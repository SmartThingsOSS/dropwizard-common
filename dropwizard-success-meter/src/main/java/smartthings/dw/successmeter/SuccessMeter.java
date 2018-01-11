package smartthings.dw.successmeter;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface SuccessMeter {

    String baseName() default "";

    String successSuffix() default "success";

    String failedSuffix() default "failed";

}
