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
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.services.AccountService;

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
			final AccountService accountService,
			final AccountRoleExtractor roleExtractor,
			final ResourceService resourceService) {
		super(accountService, roleExtractor, resourceService);
	}
	// @formatter:on

	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccount(
			@RequestParam(name = "username", required = false, defaultValue = "") String username) {
		String principalName = ContextProvider.getPrincipalName();
		Role principalRole = ContextProvider.getPrincipalRole();
		Account account;

		if (!StringHelper.hasLength(username) || principalName.equals(username)) {
			return obtainPrincipal();
		}

		account = baseRepository.findById(username, Account.class);

		if (account == null) {
			return sendNotFound(NOT_FOUND);
		}

		Class<? extends Account> type = accountService.getClassFromRole(account.getRole());

		if (!account.isActive() && !principalRole.equals(Role.ADMIN)) {
			return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
		}

		if (!principalRole.canRead(account.getRole())) {
			return unauthorize(ACCESS_DENIED);
		}

		return ResponseEntity.ok(produce(account, modelsDescriptor.getModelClass(type)));
	}

	public ResponseEntity<?> obtainPrincipal() {
		Account principal = baseRepository.findById(ContextProvider.getPrincipalName(), Account.class);

		if (principal == null) {
			return sendNotFound(NOT_FOUND);
		}

		if (!principal.isActive()) {
			return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
		}

		return ResponseEntity.ok(produce(principal, modelsDescriptor.getModelClass(principal.getClass())));
	}

}
