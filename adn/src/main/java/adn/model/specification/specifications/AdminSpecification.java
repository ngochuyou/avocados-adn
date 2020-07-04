/**
 * 
 */
package adn.model.specification.specifications;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.ModelResult;
import adn.model.entities.Admin;
import adn.model.specification.CompositeSpecification;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class AdminSpecification extends CompositeSpecification<Admin> {

	@Override
	public ModelResult<Admin> isSatisfiedBy(Admin instance) {
		// TODO Auto-generated method stub
		return instance.getContractDate() == null
				? ModelResult.error(Set.of(400), instance, Map.of("contractDate", "Contract date can not be empty"))
				: ModelResult.success(instance);
	}

}
