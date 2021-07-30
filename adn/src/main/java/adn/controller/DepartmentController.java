/**
 * 
 */
package adn.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws NoSuchFieldException {
		if (columns.isEmpty()) {
			Map<String, Object> chief = departmentService.getDepartmentChief(departmentId,
					ContextProvider.getPrincipalRole());

			return send(chief, CHIEF_NOT_FOUND);
		}

		Map<String, Object> chief = departmentService.getDepartmentChief(departmentId, columns,
				ContextProvider.getPrincipalRole());

		return send(chief, CHIEF_NOT_FOUND);
	}

	@Transactional(readOnly = true)
	@GetMapping("/chiefs")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> getDepartmentChiefs(
			@RequestParam(name = "ids", required = true) List<UUID> ids,
			@RequestParam(name = "columns", required = true) List<String> columns) throws NoSuchFieldException {
		List<Map<String, Object>> chiefs = departmentService.getDepartmentChiefs(ids.toArray(new UUID[ids.size()]),
				columns, ContextProvider.getPrincipalRole());

		return ResponseEntity.ok(chiefs);
	}

	@Transactional(readOnly = true)
	@GetMapping("/count")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> getPersonnelCounts(
			@RequestParam(name = "ids", required = true) List<UUID> ids) {
		// @formatter:off
		return makeStaleWhileRevalidate(
				departmentService.countPersonnel(ids.toArray(new UUID[ids.size()])),
				1, TimeUnit.DAYS,
				3, TimeUnit.DAYS);
		// @formatter:on
	}

	@Transactional(readOnly = true)
	@GetMapping("/personnel-list/{departmentId}")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> getPersonnelList(
			@PathVariable(name = "departmentId", required = true) UUID departmentId,
			@PageableDefault(size = 5) Pageable paging,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns) throws NoSuchFieldException {
		List<Map<String, Object>> list = departmentService.getPersonnelListByDepartmentId(departmentId, columns, paging,
				ContextProvider.getPrincipalRole());

		return ResponseEntity.ok(list);
	}

}
