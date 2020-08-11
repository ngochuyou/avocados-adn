/**
 * 
 */
package adn.service.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import adn.model.entities.Factor;
import adn.service.ApplicationGenericService;
import adn.service.GenericService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@GenericService(target = Factor.class)
public class FactorService implements ApplicationGenericService<Factor> {

	@Override
	public Factor doProcedure(Factor model) {
		// TODO Auto-generated method stub
		model.setName(Strings.normalizeString(model.getName()));
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));
		model.setUpdatedBy(Strings.removeSpaces(model.getUpdatedBy()));

		return model;
	}

	@Override
	public Factor doInsertionProcedure(Factor model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

	@Override
	public Factor doUpdateProcedure(Factor model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setUpdatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

}
