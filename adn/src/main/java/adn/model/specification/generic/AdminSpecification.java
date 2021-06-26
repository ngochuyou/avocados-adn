/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import adn.model.DatabaseInteractionResult;
import adn.model.Generic;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Admin.class)
public class AdminSpecification extends AccountSpecification<Admin> {

	@Override
	public DatabaseInteractionResult<Admin> isSatisfiedBy(Serializable id, Admin instance) {
		DatabaseInteractionResult<Admin> result = super.isSatisfiedBy(id, instance);

		if (instance.getContractDate() == null) {
			result.getMessages().put("contractDate", "Contract date can not be empty");
			result.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return result;
	}

}
