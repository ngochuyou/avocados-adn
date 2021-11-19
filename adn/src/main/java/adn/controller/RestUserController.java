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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.model.entities.User;
import adn.model.entities.metadata._User;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;
import adn.service.UserRoleExtractor;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.services.UserService;
import adn.service.services.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/user")
public class RestUserController extends UserController {
	// @formatter:off
	@Autowired
	public RestUserController(
			final UserService accountService,
			final AuthenticationService authService,
			final UserRoleExtractor roleExtractor,
			final ResourceService resourceService,
			final JavaMailSender mailSender) {
		super(accountService, roleExtractor, resourceService, authService, mailSender);
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
			return bad(ssee.getMessage());
		}
	}

	@GetMapping("/{username}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> obtainAccount(@PathVariable(name = "username", required = true) String username,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws UnauthorizedCredential {
		try {
			return doObtainAccount(username, columns);
		} catch (NoSuchFieldException ssee) {
			return bad(ssee.getMessage());
		}
	}

	@PatchMapping("/deactivate/{username}")
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public ResponseEntity<?> deactivateAccount(@PathVariable(name = "username", required = true) String username) {
		authService.assertPersonnelDepartment();

		if (genericRepository.countById(User.class, username) == 0) {
			return notFound(Common.notfound());
		}

		Result<User> result = accountService.deactivateAccount(username, true);

		if (result.isOk()) {
			return send(String.format("Deactivated %s", username), null);
		}

		return bad(result.getMessages());
	}

	protected ResponseEntity<?> obtainPrincipal(Collection<String> requestedColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		String username = ContextProvider.getPrincipalName();

		if (requestedColumns.size() == 0) {
			Optional<User> optional = genericRepository.findById(User.class, username);

			if (optional.isEmpty()) {
				return notFound();
			}

			User account = optional.get();

			if (!account.isActive()) {
				return ResponseEntity.status(HttpStatus.LOCKED).body(Common.LOCKED);
			}

			return send(account, accountService.getClassFromRole(account.getRole()), null, owner());
		}

		requestedColumns.add(_User.role);

		Map<String, Object> cols = crudService.readById(username, User.class, requestedColumns, owner());

		if (cols == null) {
			return notFound();
		}

		if (((Role) cols.get(_User.role)) == Role.PERSONNEL) {
			cols.put("departmentId", authService.getPrincipalDepartment());
		}

		return ResponseEntity.ok(cols);
	}

	protected ResponseEntity<?> doObtainAccount(String username, Collection<String> requestedColumns)
			throws NoSuchFieldException, UnauthorizedCredential {
		Role principalRole = ContextProvider.getPrincipalRole();

		if (requestedColumns.size() == 0) {
			Optional<User> optional = genericRepository.findById(User.class, username);

			if (optional.isEmpty()) {
				return notFound(Common.notfound());
			}

			User account = optional.get();

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

		if (!columns.contains(_User.active)) {
			columns.add(_User.active);
		}

		if (!columns.contains(_User.role)) {
			columns.add(_User.role);
		}

		Map<String, Object> fetchedRow = crudService.readById(username, User.class, columns,
				getPrincipalCredential());

		if (fetchedRow == null) {
			return notFound(Common.notfound());
		}

		if (principalRole.equals(Role.PERSONNEL) && authService.isPersonnelDepartment()) {
			return ResponseEntity.ok(extractRequestedColumns(requestedColumns, fetchedRow));
		}

		Boolean isActive = (Boolean) (fetchedRow.get(_User.active));

		if (!isActive) {
			return ResponseEntity.status(HttpStatus.LOCKED).body(Common.LOCKED);
		}

		if (!principalRole.canRead((Role) (fetchedRow.get(_User.role)))) {
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
