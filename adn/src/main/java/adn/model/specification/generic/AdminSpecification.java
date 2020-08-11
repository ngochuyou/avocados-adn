/**
 * 
 */
package adn.model.specification.generic;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import adn.model.Result;
import adn.model.entities.Admin;
import adn.model.specification.GenericSpecification;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Component
@GenericSpecification(target = Admin.class)
public class AdminSpecification implements Specification<Admin> {

	@Override
	public Result<Admin> isSatisfiedBy(Admin instance) {
		// TODO Auto-generated method stub
		return instance.getContractDate() == null
				? Result.error(Set.of(400), instance, Map.of("contractDate", "Contract date can not be empty"))
				: Result.success(instance);
	}

}