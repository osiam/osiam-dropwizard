package org.tarent.osiam.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RestrictedTo-Annotation to restrict access to resources to certain user groups.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface RestrictedTo {
    /**
     * Returns an array of Strings with the allowed group, whom may access an annotated resource.
     *
     * @return array of Strings.
     */
    String[] value() default {};
}
