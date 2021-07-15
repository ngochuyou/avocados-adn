/**
 * 
 */
package adn.controller;

import static adn.helpers.ArrayHelper.from;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.service.services.DepartmentService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/department")
public class DepartmentController extends BaseController {

	protected static final String CHIEF_NOT_FOUND = "Unable to find department chief";

	private final DepartmentService departmentService;

	@Autowired
	public DepartmentController(DepartmentService departmentService) {
		this.departmentService = departmentService;
	}

	@Transactional(readOnly = true)
	@GetMapping("/chief/{departmentId}")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> getDepartmentChief(
			@PathVariable(name = "id", required = true) UUID departmentId,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) {
		if (columns.isEmpty()) {
			Map<String, Object> chief = departmentService.getDepartmentChief(departmentId,
					ContextProvider.getPrincipalRole());

			return send(chief, CHIEF_NOT_FOUND);
		}

		try {
			Map<String, Object> chief = departmentService.getDepartmentChief(departmentId, from(columns),
					ContextProvider.getPrincipalRole());

			return send(chief, CHIEF_NOT_FOUND);
		} catch (SQLSyntaxErrorException ssee) {
			return sendBadRequest(ssee.getMessage());
		}
	}

	@Transactional(readOnly = true)
	@GetMapping("/chiefs")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> getDepartmentChiefs(
			@RequestParam(name = "ids", required = true) List<UUID> ids,
			@RequestParam(name = "columns", required = true) List<String> columns) {
		try {
			List<Map<String, Object>> chiefs = departmentService.getDepartmentChiefs(ids.toArray(new UUID[ids.size()]),
					from(columns), ContextProvider.getPrincipalRole());

			return ResponseEntity.ok(chiefs);
		} catch (SQLSyntaxErrorException ssee) {
			return sendBadRequest(ssee.getMessage());
		}

	}

	@Transactional(readOnly = true)
	@GetMapping("/count")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> getPersonnelCounts(
			@RequestParam(name = "ids", required = true) List<UUID> ids) {
		return ResponseEntity.ok(departmentService.countPersonnel(ids.toArray(new UUID[ids.size()])));
	}

}
