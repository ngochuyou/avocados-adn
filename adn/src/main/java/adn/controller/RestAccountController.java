/**
 * 
 */
package adn.controller;

import static adn.helpers.ArrayHelper.from;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Account;
import adn.service.internal.AccountRoleExtractor;
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
	public ResponseEntity<?> obtainAccountOrPrincipal(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) {
		try {
			String principalName = ContextProvider.getPrincipalName();

			if (!StringHelper.hasLength(username) || principalName.equals(username)) {
				return obtainPrincipal(from(columns));
			}

			return doObtainAccount(username, columns);
		} catch (SQLSyntaxErrorException ssee) {
			return sendBadRequest(ssee.getMessage());
		}
	}

	@GetMapping("/{username}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccount(@PathVariable(name = "username", required = true) String username,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) {
		try {
			return doObtainAccount(username, columns);
		} catch (SQLSyntaxErrorException ssee) {
			return sendBadRequest(ssee.getMessage());
		}
	}

	@GetMapping("/deact/{username}")
	@Secured("ROLE_ADMIN")
	@Transactional
	public ResponseEntity<?> deactivateAccount(@PathVariable(name = "username", required = true) String username) {
		Account account = baseRepository.findById(username, Account.class);

		if (account == null) {
			return sendNotFound(NOT_FOUND);
		}

		DatabaseInteractionResult<Account> result = crudService.deactivate(username, account, Account.class);

		if (result.isOk()) {
			currentSession(ss -> ss.flush());
			return ResponseEntity.ok(String.format("Deactivated %s", username));
		}

		return fails(result.getMessages());
	}

	protected ResponseEntity<?> obtainPrincipal(String[] requestedColumns) throws SQLSyntaxErrorException {
		String username = ContextProvider.getPrincipalName();

		if (requestedColumns.length == 0) {
			Account model = baseRepository.findById(username, Account.class);

			if (model == null) {
				return sendNotFound(NOT_FOUND);
			}

			if (!model.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
			}

			return send(model, accountService.getClassFromRole(model.getRole()), null);
		}

		return super.<Map<String, Object>>send(crudService.find(username, Account.class, requestedColumns), NOT_FOUND);
	}

	protected ResponseEntity<?> doObtainAccount(String username, List<String> requestedColumns)
			throws SQLSyntaxErrorException {
		Role principalRole = ContextProvider.getPrincipalRole();

		if (requestedColumns.size() == 0) {
			Account model = baseRepository.findById(username, Account.class);

			if (model == null) {
				return sendNotFound(NOT_FOUND);
			}

			if (principalRole.equals(Role.ADMIN)) {
				return send(model, accountService.getClassFromRole(model.getRole()), null);
			}

			if (!model.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
			}

			if (!principalRole.canRead(model.getRole())) {
				return unauthorize(ACCESS_DENIED);
			}

			return send(model, accountService.getClassFromRole(model.getRole()), null);
		}

		List<String> columns = new ArrayList<>(requestedColumns);

		if (!columns.contains(Account.ACTIVE_FIELD_NAME)) {
			columns.add(Account.ACTIVE_FIELD_NAME);
		}

		if (!columns.contains(Account.ROLE_FIELD_NAME)) {
			columns.add(Account.ROLE_FIELD_NAME);
		}

		Map<String, Object> fetchedRow = crudService.find(username, Account.class, from(columns));

		if (fetchedRow == null) {
			return sendNotFound(NOT_FOUND);
		}

		if (principalRole.equals(Role.ADMIN)) {
			return ResponseEntity.ok(extractRequestedColumns(requestedColumns, fetchedRow));
		}

		Boolean isActive = (Boolean) (fetchedRow.get(Account.ACTIVE_FIELD_NAME));

		if (!isActive) {
			return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
		}

		if (!principalRole.canRead((Role) (fetchedRow.get(Account.ROLE_FIELD_NAME)))) {
			return unauthorize(ACCESS_DENIED);
		}

		return ResponseEntity.ok(extractRequestedColumns(requestedColumns, fetchedRow));
	}

	private Map<String, Object> extractRequestedColumns(List<String> requestedColumns, Map<String, Object> fetchedRow) {
		return requestedColumns.stream().map(col -> Utils.Entry.<String, Object>entry(col, fetchedRow.get(col)))
				.collect(HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
						HashMap::putAll);
	}

}
