/**
 * 
 */
package adn.service.resource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ngoc Huy
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LocalResource {

	String name() default "";

	Class<?> systemType();

	Class<?> instantiatorClass() default Void.class;

}
