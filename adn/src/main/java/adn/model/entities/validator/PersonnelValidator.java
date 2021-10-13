/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.application.Common;
import adn.application.Result;
import adn.dao.generic.GenericRepository;
import adn.model.Generic;
import adn.model.entities.Personnel;
import adn.model.entities.metadata._Personnel;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Personnel.class)
@Component
public class PersonnelValidator extends UserValidator<Personnel> {

	private static final String MISSING_DEPARTMENT = Common.notEmpty("Department information");

	public PersonnelValidator(GenericRepository genericRepository) {
		super(genericRepository);
	}

	@Override
	public Result<Personnel> isSatisfiedBy(Session session, Serializable id, Personnel instance) {
		Result<Personnel> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getDepartment() == null) {
			result.bad(_Personnel.department, MISSING_DEPARTMENT);
		}

		return result;
	}

}
