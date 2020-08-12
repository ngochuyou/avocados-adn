/**
 * 
 */
package adn.service.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import adn.model.Genetized;
import adn.model.entities.Personnel;
import adn.service.GenericService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(gene = Personnel.class)
public class PersonnelService implements GenericService<Personnel> {

	@Override
	public Personnel executeDefaultProcedure(Personnel model) {
		// TODO Auto-generated method stub
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));

		return model;
	}

	@Override
	public Personnel executeInsertionProcedure(Personnel model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

}
