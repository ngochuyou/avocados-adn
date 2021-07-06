/**
 * 
 */
package adn.model.factory.property.production;

import java.util.function.Function;

import adn.model.AbstractModel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface SecuredProperty<T extends AbstractModel> {

	Class<T> getEntityType();

	String getPropertyName();

	String getPropertyAlternativeName();

	Role getRole();

	Function<Object, Object> getFunction();

}
