/**
 * 
 */
package adn.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface GenericService {

	public Class<? extends Model> target() default Model.class;

}
