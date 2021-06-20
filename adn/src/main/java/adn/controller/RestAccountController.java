/**
 * 
 */
package adn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.model.entities.Account;
import adn.model.factory.extraction.AccountRoleExtractor;
import adn.service.Role;
import adn.service.resource.ResourceManager;
import adn.service.services.AccountService;
import adn.service.services.ResourceService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/account")
public class RestAccountController extends AccountController {

	// @formatter:off
	@Autowired
	public RestAccountController(
			AccountService accountService,
			AccountRoleExtractor roleExtractor,
			ResourceService resourceService,
			ResourceManager resourceManager) {
		super(accountService, roleExtractor, resourceService, resourceManager);
	}
	// @formatter:on

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
				return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
			}

			return ResponseEntity.ok(produce(account, modelsDescriptor.getModelClass(clazz)));
		}

		account = dao.findById(username, Account.class);

		if (account == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(NOT_FOUND);
		}

		if (account.isActive()) {
			return ResponseEntity.ok(produce(account,
					modelsDescriptor.getModelClass(accountService.getClassFromRole(account.getRole()))));
		}

		return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
	}

}
