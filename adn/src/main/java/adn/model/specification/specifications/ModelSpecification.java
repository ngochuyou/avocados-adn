/**
 * 
 */
package adn.model.specification.specifications;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.Model;
import adn.model.ModelResult;
import adn.model.specification.CompositeSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ModelSpecification extends CompositeSpecification<Model> {

	@Override
	public ModelResult<Model> isSatisfiedBy(Model instance) {
		// TODO Auto-generated method stub
		boolean flag = true;
		Set<Integer> status = new HashSet<>();
		Map<String, String> messageSet = new HashMap<>();

		if (instance.getId() == null) {
			status.add(400);
			messageSet.put("id", "Id can not be empty");
			flag = false;
		}

		return flag ? ModelResult.success(instance) : ModelResult.error(status, instance, messageSet);
	}

}
