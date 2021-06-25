/**
 * 
 */
package adn.controller;

import javax.transaction.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import adn.application.context.ContextProvider;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Provider;
import adn.service.internal.Role;

/**
 * Basic controller for factors whose business are fairly simple
 * 
 * @author Ngoc Huy
 *
 */
@Controller
public class FactorController extends BaseController {

	@Transactional
	@PostMapping("/provider")
	public @ResponseBody ResponseEntity<?> createProvider(@RequestBody Provider provider) {
		Role role = ContextProvider.getPrincipalRole();

		if (!role.equals(Role.ADMIN) && !role.equals(Role.PERSONNEL)) {
			return unauthorize(ACCESS_DENIED);
		}

		DatabaseInteractionResult<Provider> result = crudService.create(provider, Provider.class);

		return sendFromDatabaseInteractionResult(result);
	}

}
