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
import adn.model.entities.Personnel;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Genetized(entityGene = Personnel.class)
public class PersonnelDAO extends BaseDAO implements GenericDAO<Personnel> {

	@Override
	public Personnel defaultBuild(Personnel model) {
		// TODO Auto-generated method stub
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));

		return model;
	}

	@Override
	public Personnel insertionBuild(Personnel model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());
		
		return model;
	}

}
