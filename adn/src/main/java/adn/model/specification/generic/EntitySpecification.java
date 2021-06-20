/**
 * 
 */
package adn.model.specification.generic;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.DatabaseInteractionResult;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Entity.class)
public class EntitySpecification<T extends Entity> implements Specification<T> {

	@Override
	public DatabaseInteractionResult<T> isSatisfiedBy(T instance) {
		DatabaseInteractionResult<T> result = new DatabaseInteractionResult<>(instance);

		if (instance.getId() == null) {
			result.getMessages().put("id", "Id can not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
