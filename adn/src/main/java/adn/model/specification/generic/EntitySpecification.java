/**
 * 
 */
package adn.model.specification.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.Entity;
import adn.model.Result;
import adn.model.specification.GenericSpecification;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Entity.class)
public class EntitySpecification implements Specification<Entity> {

	@Override
	public Result<Entity> isSatisfiedBy(Entity instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Set<Integer> status = new HashSet<>();
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null) {
			status.add(400);
			messageSet.put("id", "Id can not be empty");
			flag = false;
		}

		return flag ? Result.success(instance) : Result.error(status, instance, messageSet);
	}

}
