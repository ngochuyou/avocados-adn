/**
 * 
 */
package adn.model.specification.generic;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.Genetized;
import adn.model.Result;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Genetized(entityGene = Admin.class)
public class AdminSpecification extends AccountSpecification<Admin> {

	@Override
	public Result<Admin> isSatisfiedBy(Admin instance) {
		// TODO Auto-generated method stub
		Result<Admin> result = super.isSatisfiedBy(instance);

		if (instance.getContractDate() == null) {
			result.getMessageSet().put("contractDate", "Contract date can not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
