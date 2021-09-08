/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static adn.application.context.builders.CredentialFactory.owner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.application.context.ContextProvider;
import adn.dao.generic.Result;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.entities.metadata._Account;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.AccountRoleExtractor;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.services.AccountService;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/account")
public class RestAccountController extends AccountController {

	private final AuthenticationService authService;

	// @formatter:off
	@Autowired
	public RestAccountController(
			final AccountService accountService,
			final AuthenticationService authService,
			final AccountRoleExtractor roleExtractor,
			final ResourceService resourceService) {
		super(accountService, roleExtractor, resourceService);
		this.authService = authService;
	}
	// @formatter:on
	@GetMapping
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccountOrPrincipal(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws UnauthorizedCredential {
		try {
			String principalName = ContextProvider.getPrincipalName();

			if (!StringHelper.hasLength(username) || principalName.equals(username)) {
				return obtainPrincipal(columns);
			}

			return doObtainAccount(username, columns);
		} catch (NoSuchFieldException ssee) {
			return sendBad(ssee.getMessage());
		}
	}

	@GetMapping("/{username}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccount(@PathVariable(name = "username", required = true) String username,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws UnauthorizedCredential {
		try {
			return doObtainAccount(username, columns);
		} catch (NoSuchFieldException ssee) {
			return sendBad(ssee.getMessage());
		}
	}

	@PatchMapping("/deactivate/{username}")
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> deactivateAccount(@PathVariable(name = "username", required = true) String username) {
		authService.assertPersonnelDepartment();

		if (baseRepository.countById(Account.class, username) == 0) {
			return sendNotFound(Common.NOT_FOUND);
		}

		Result<Account> result = accountService.deactivateAccount(username, true);

		if (result.isOk()) {
			return send(String.format("Deactivated %s", username), null);
		}

		return sendBad(result.getMessages());
	}

	protected ResponseEntity<?> obtainPrincipal(Collection<String> requestedColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		String username = ContextProvider.getPrincipalName();

		if (requestedColumns.size() == 0) {
			Optional<Account> optional = baseRepository.findById(Account.class, username);

			if (optional.isEmpty()) {
				return sendNotFound(Common.NOT_FOUND);
			}

			Account account = optional.get();

			if (!account.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(Common.LOCKED);
			}

			return send(account, accountService.getClassFromRole(account.getRole()), null, owner());
		}

		requestedColumns.add(_Account.role);

		Map<String, Object> cols = crudService.readById(username, Account.class, requestedColumns, owner());

		if (cols == null) {
			return sendNotFound(Common.NOT_FOUND);
		}

		if (((Role) cols.get(_Account.role)) == Role.PERSONNEL) {
			cols.put("departmentId", authService.getPrincipalDepartment());
		}

		return ResponseEntity.ok(cols);
	}

	protected ResponseEntity<?> doObtainAccount(String username, Collection<String> requestedColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		Role principalRole = ContextProvider.getPrincipalRole();

		if (requestedColumns.size() == 0) {
			Optional<Account> optional = baseRepository.findById(Account.class, username);

			if (optional.isEmpty()) {
				return sendNotFound(Common.NOT_FOUND);
			}

			Account account = optional.get();

			if (principalRole.equals(Role.PERSONNEL) && authService.isPersonnelDepartment()) {
				return send(account, accountService.getClassFromRole(account.getRole()), null);
			}

			if (!account.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(Common.LOCKED);
			}

			if (!principalRole.canRead(account.getRole())) {
				return unauthorize(Common.ACCESS_DENIED);
			}

			return send(account, accountService.getClassFromRole(account.getRole()), null);
		}

		List<String> columns = new ArrayList<>(requestedColumns);

		if (!columns.contains(_Account.active)) {
			columns.add(_Account.active);
		}

		if (!columns.contains(_Account.role)) {
			columns.add(_Account.role);
		}

		Map<String, Object> fetchedRow = crudService.readById(username, Account.class, columns,
				getPrincipalCredential());

		if (fetchedRow == null) {
			return sendNotFound(Common.NOT_FOUND);
		}

		if (principalRole.equals(Role.PERSONNEL) && authService.isPersonnelDepartment()) {
			return ResponseEntity.ok(extractRequestedColumns(requestedColumns, fetchedRow));
		}

		Boolean isActive = (Boolean) (fetchedRow.get(_Account.active));

		if (!isActive) {
			return ResponseEntity.status(HttpStatus.LOCKED).body(Common.LOCKED);
		}

		if (!principalRole.canRead((Role) (fetchedRow.get(_Account.role)))) {
			return unauthorize(Common.ACCESS_DENIED);
		}

		return ResponseEntity.ok(extractRequestedColumns(requestedColumns, fetchedRow));
	}

	private Map<String, Object> extractRequestedColumns(Collection<String> requestedColumns,
			Map<String, Object> fetchedRow) {
		return requestedColumns.stream().map(col -> Utils.Entry.<String, Object>entry(col, fetchedRow.get(col)))
				.collect(HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
						HashMap::putAll);
	}

}
