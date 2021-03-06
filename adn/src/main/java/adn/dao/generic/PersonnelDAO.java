/**
 * 
 */
package adn.dao.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

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
	public Personnel defaultBuild(Personnel model) {
		// TODO Auto-generated method stub
		model = super.defaultBuild(model);

		if (StringHelper.hasLength(model.getCreatedBy())) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			model.setCreatedBy(
					authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());
		}

		return model;
	}

	@Override
	public Personnel updateBuild(Personnel model) {
		// TODO Auto-generated method stub
		model = super.updateBuild(model);

		Personnel persistence = sessionFactory.getCurrentSession().load(Personnel.class, model.getId());

		if (!StringHelper.hasLength(persistence.getCreatedBy())) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

			persistence.setCreatedBy(
					authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());
		}

		return model;
	}

}
