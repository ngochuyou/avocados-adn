/**
 * 
 */
package adn.model.factory;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.entities.Entity;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface EntityExtractor<T extends Entity, M extends Model> {

	final TypeHelper reflector = ContextProvider.getApplicationContext().getBean(TypeHelper.class);

	default T extract(M model, T entity) throws NullPointerException {
		return entity;
	};

}
