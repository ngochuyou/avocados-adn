/**
 * 
 */
package adn.model.specification.specifications;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.ModelResult;
import adn.model.entities.Personnel;
import adn.model.specification.CompositeSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class PersonnelSpecification extends CompositeSpecification<Personnel> {

	@Override
	public ModelResult<Personnel> isSatisfiedBy(Personnel instance) {
		// TODO Auto-generated method stub
		return instance.getCreatedBy() == null
				? ModelResult.error(Set.of(400), instance, Map.of("createdBy", "Created by can not be empty"))
				: ModelResult.success(instance);
	}

}
