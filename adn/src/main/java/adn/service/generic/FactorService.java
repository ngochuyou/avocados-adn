/**
 * 
 */
package adn.service.generic;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import adn.model.Genetized;
import adn.model.entities.Factor;
import adn.service.GenericService;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Service
@Genetized(gene = Factor.class)
public class FactorService implements GenericService<Factor> {

	@Override
	public Factor executeDefaultProcedure(Factor model) {
		// TODO Auto-generated method stub
		model.setName(Strings.normalizeString(model.getName()));
		model.setCreatedBy(Strings.removeSpaces(model.getCreatedBy()));
		model.setUpdatedBy(Strings.removeSpaces(model.getUpdatedBy()));

		return model;
	}

	@Override
	public Factor executeInsertionProcedure(Factor model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setCreatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

	@Override
	public Factor executeUpdateProcedure(Factor model) {
		// TODO Auto-generated method stub
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		model.setUpdatedBy(authentication instanceof AnonymousAuthenticationToken ? null : authentication.getName());

		return model;
	}

}
