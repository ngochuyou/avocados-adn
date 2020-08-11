/**
 * 
 */
package adn.service.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import adn.model.entities.Personnel;
import adn.service.ApplicationGenericService;
import adn.service.GenericService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@GenericService(target = Personnel.class)
public class PersonnelService implements ApplicationGenericService<Personnel> {

	@Override
	public Personnel doProcedure(Personnel model) {
		// TODO Auto-generated method stub
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));

		return model;
	}

	@Override
	public Personnel doInsertionProcedure(Personnel model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

}
