/**
 * 
 */
package adn.controller;

import static adn.application.context.ContextProvider.getPrincipalCredential;
import static adn.model.entities.Account.ACTIVE_FIELD_NAME;
import static adn.model.entities.Account.ROLE_FIELD_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import adn.application.context.ContextProvider;
import adn.dao.generic.Result;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.AccountRoleExtractor;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.services.AccountService;
import adn.service.services.DepartmentService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/account")
public class RestAccountController extends AccountController {

	private final DepartmentService departmentService;

	// @formatter:off
	@Autowired
	public RestAccountController(
			final AccountService accountService,
			final DepartmentService departmentService,
			final AccountRoleExtractor roleExtractor,
			final ResourceService resourceService) {
		super(accountService, roleExtractor, resourceService);
		this.departmentService = departmentService;
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
			return sendBadRequest(ssee.getMessage());
		}
	}

	@GetMapping("/{username}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccount(@PathVariable(name = "username", required = true) String username,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws UnauthorizedCredential {
		try {
			return doObtainAccount(username, columns);
		} catch (NoSuchFieldException ssee) {
			return sendBadRequest(ssee.getMessage());
		}
	}

	@PatchMapping("/deactivate/{username}")
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> deactivateAccount(@PathVariable(name = "username", required = true) String username) {
		departmentService.assertPersonnelDepartment();

		if (baseRepository.countById(username, Account.class) == 0) {
			return sendNotFound(NOT_FOUND);
		}

		Result<Account> result = accountService.deactivateAccount(username, true);

		if (result.isOk()) {
			return send(String.format("Deactivated %s", username), null);
		}

		return sendBadRequest(result.getMessages());
	}

	protected ResponseEntity<?> obtainPrincipal(Collection<String> requestedColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		String username = ContextProvider.getPrincipalName();

		if (requestedColumns.size() == 0) {
			Account model = baseRepository.findById(username, Account.class);

			if (model == null) {
				return sendNotFound(NOT_FOUND);
			}

			if (!model.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
			}

			return send(model, accountService.getClassFromRole(model.getRole()), null);
		}

		requestedColumns.add(ROLE_FIELD_NAME);

		Map<String, Object> cols = crudService.find(username, Account.class, requestedColumns,
				getPrincipalCredential());

		if (cols == null) {
			return sendNotFound(NOT_FOUND);
		}

		if (((Role) cols.get(ROLE_FIELD_NAME)) == Role.PERSONNEL) {
			cols.put("departmentId", departmentService.getPrincipalDepartment());
		}

		return ResponseEntity.ok(cols);
	}

	protected ResponseEntity<?> doObtainAccount(String username, Collection<String> requestedColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		Role principalRole = ContextProvider.getPrincipalRole();

		if (requestedColumns.size() == 0) {
			Account model = baseRepository.findById(username, Account.class);

			if (model == null) {
				return sendNotFound(NOT_FOUND);
			}

			if (principalRole.equals(Role.PERSONNEL) && departmentService.isPersonnelDepartment()) {
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

		if (!columns.contains(ACTIVE_FIELD_NAME)) {
			columns.add(ACTIVE_FIELD_NAME);
		}

		if (!columns.contains(ROLE_FIELD_NAME)) {
			columns.add(ROLE_FIELD_NAME);
		}

		Map<String, Object> fetchedRow = crudService.find(username, Account.class, columns, getPrincipalCredential());

		if (fetchedRow == null) {
			return sendNotFound(NOT_FOUND);
		}

		if (principalRole.equals(Role.PERSONNEL) && departmentService.isPersonnelDepartment()) {
			return ResponseEntity.ok(extractRequestedColumns(requestedColumns, fetchedRow));
		}

		Boolean isActive = (Boolean) (fetchedRow.get(ACTIVE_FIELD_NAME));

		if (!isActive) {
			return ResponseEntity.status(HttpStatus.LOCKED).body(LOCKED);
		}

		if (!principalRole.canRead((Role) (fetchedRow.get(ROLE_FIELD_NAME)))) {
			return unauthorize(ACCESS_DENIED);
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
