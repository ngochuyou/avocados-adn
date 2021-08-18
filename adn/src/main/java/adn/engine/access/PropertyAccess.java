/**
 * 
 */
package adn.engine.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Ngoc Huy
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyAccess {

	Class<?> clazz();

	Type type();

	public enum Type {

		GETTER, SETTER;

	}

}
