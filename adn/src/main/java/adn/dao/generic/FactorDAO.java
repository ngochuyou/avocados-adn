/**
 * 
 */
package adn.dao.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import adn.model.Genetized;
import adn.model.entities.Factor;
import adn.utilities.Strings;

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
		model.setName(Strings.normalizeString(model.getName()));
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));
		model.setUpdatedBy(Strings.removeSpaces(model.getUpdatedBy()));

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
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model = super.updateBuild(model);
		model.setUpdatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

}
