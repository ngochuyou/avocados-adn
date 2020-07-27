/**
 * 
 */
package adn.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import adn.model.Entity;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface EMFactory {

	public Class<? extends Entity> entityClass() default Entity.class;

	public Class<? extends Model> modelClass() default Model.class;

}
