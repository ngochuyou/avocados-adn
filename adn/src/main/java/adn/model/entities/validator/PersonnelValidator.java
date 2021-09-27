/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.dao.generic.Result;
import adn.model.Generic;
import adn.model.entities.Personnel;
import adn.model.entities.metadata._Personnel;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Personnel.class)
@Component
public class PersonnelValidator extends AccountValidator<Personnel> {

	private static final String MISSING_OPERATOR = Common.notEmpty("Operator information");
	private static final String MISSING_DEPARTMENT = Common.notEmpty("Department information");

	@Override
	public Result<Personnel> isSatisfiedBy(Session session, Serializable id, Personnel instance) {
		Result<Personnel> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getCreatedBy() == null) {
			result.bad().getMessages().put(_Personnel.createdBy, MISSING_OPERATOR);
		}

		if (instance.getDepartment() == null) {
			result.bad().getMessages().put(_Personnel.department, MISSING_DEPARTMENT);
		}

		return result;
	}

}
