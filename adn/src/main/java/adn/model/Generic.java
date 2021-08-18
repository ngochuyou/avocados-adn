/**
 * 
 */
package adn.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Generic {

	public Class<? extends DomainEntity> entityGene() default DomainEntity.class;

	public Class<? extends Model> modelGene() default Model.class;

}
