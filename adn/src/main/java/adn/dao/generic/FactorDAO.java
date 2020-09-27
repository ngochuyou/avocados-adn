/**
 * 
 */
package adn.dao.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import adn.dao.BaseDAO;
import adn.dao.GenericDAO;
import adn.model.Genetized;
import adn.model.entities.Factor;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Factor.class)
public class FactorDAO extends BaseDAO implements GenericDAO<Factor> {

	@Override
	public Factor defaultBuild(Factor model) {
		// TODO Auto-generated method stub
		model.setName(Strings.normalizeString(model.getName()));
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));
		model.setUpdatedBy(Strings.removeSpaces(model.getUpdatedBy()));

		return model;
	}

	@Override
	public Factor insertionBuild(Factor model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

	@Override
	public Factor updateBuild(Factor model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setUpdatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

}
