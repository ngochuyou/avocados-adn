/**
 * 
 */
package adn.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.helpers.Role;
import adn.helpers.StringHelper;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/account")
public class RestAccountController extends AccountController {

	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccount(
			@RequestParam(name = "username", required = false, defaultValue = "") String username) {
		String principalName = ContextProvider.getPrincipalName();
		Role principalRole = ContextProvider.getPrincipalRole();
		Account account;
		Class<? extends Account> clazz;

		if (!StringHelper.hasLength(username) || principalName.equals(username)) {
			clazz = accountService.getClassFromRole(principalRole);
			account = dao.findById(principalName, clazz);

			if (!account.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(locked);
			}

			return ResponseEntity.ok(produce(account, modelManager.getModelClass(clazz)));
		}

		account = dao.findById(username, Account.class);

		if (account == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
		}

		if (account.isActive()) {
			return ResponseEntity.ok(
					produce(account, modelManager.getModelClass(accountService.getClassFromRole(account.getRole()))));
		}

		return ResponseEntity.status(HttpStatus.LOCKED).body(locked);
	}

}
