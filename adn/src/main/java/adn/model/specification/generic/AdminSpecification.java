/**
 * 
 */
package adn.model.specification.generic;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.DatabaseInteractionResult;
import adn.model.Genetized;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Admin.class)
public class AdminSpecification extends AccountSpecification<Admin> {

	@Override
	public DatabaseInteractionResult<Admin> isSatisfiedBy(Admin instance) {
		DatabaseInteractionResult<Admin> result = super.isSatisfiedBy(instance);

		if (instance.getContractDate() == null) {
			result.getMessages().put("contractDate", "Contract date can not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}
		
		return result;
	}

}
