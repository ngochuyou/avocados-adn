/**
 * 
 */
package adn.dao.generic;

import org.springframework.stereotype.Repository;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.model.Genetized;
import adn.model.entities.Personnel;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Personnel.class)
public class PersonnelDAO extends AccountDAO<Personnel> {

	@Override
	public Personnel insertionBuild(Personnel model) {
		// TODO Auto-generated method stub
		model = super.insertionBuild(model);

		model.setCreatedBy(ContextProvider.getPrincipalName());

		return model;
	}

	@Override
	public Personnel updateBuild(Personnel model) {
		// TODO Auto-generated method stub
		model = super.updateBuild(model);

		Personnel persistence = sessionFactory.getCurrentSession().get(Personnel.class, model.getId());

		if (!StringHelper.hasLength(persistence.getCreatedBy())) {
			persistence.setCreatedBy(ContextProvider.getPrincipalName());
		}

		return model;
	}

}
