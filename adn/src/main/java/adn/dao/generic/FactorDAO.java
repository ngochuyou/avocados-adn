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
import adn.model.entities.Factor;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Factor.class)
public class FactorDAO<T extends Factor> extends EntityDAO<T> {

	@Override
	public T defaultBuild(T model) {
		// TODO Auto-generated method stub
		model = super.defaultBuild(model);
		model.setName(StringHelper.normalizeString(model.getName()));
		model.setCreatedBy(StringHelper.removeSpaces(model.getCreatedBy()));
		model.setUpdatedBy(StringHelper.removeSpaces(model.getUpdatedBy()));

		return model;
	}

	@Override
	public T insertionBuild(T model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model = super.insertionBuild(model);
		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

	@Override
	public T updateBuild(T model) {
		// TODO Auto-generated method stub
		super.updateBuild(model);
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Factor persisted = sessionFactory.getCurrentSession().load(Factor.class, model.getId());
		
		if (persisted.getUpdatedBy() == null) {
			persisted.setUpdatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());
		}

		return model;
	}

}
