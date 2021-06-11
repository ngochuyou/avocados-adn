/**
 * 
 */
package adn.service.resource.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ngoc Huy
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constructor {

	/**
	 * Names of the attribute to be passed into the constructor
	 * 
	 * @return
	 */
	String[] columnNames() default { };

	/**
	 * Constructor's argument types
	 * 
	 * @return
	 */
	Class<?>[] argumentTypes() default { };

}
