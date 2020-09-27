/**
 * 
 */
package adn.model.specification.generic;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.Result;
import adn.model.entities.Entity;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Entity.class)
public class EntitySpecification<T extends Entity> implements Specification<T> {

	@Override
	public Result<T> isSatisfiedBy(T instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null) {
			messageSet.put("id", "Id can not be empty");
			flag = false;
		}

		return flag ? Result.success(instance) : Result.error(HttpStatus.BAD_REQUEST.value(), instance, messageSet);
	}

}
