/**
 * 
 */
package adn.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.model.entities.Provider;
import adn.service.services.DepartmentService;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/provider")
public class ProviderController extends DepartmentScopedController {

	private static final int COMMON_CACHE_MAXAGE = 1;

	@Autowired
	public ProviderController(DepartmentService departmentService) {
		super(departmentService);
	}

	@GetMapping
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getAllProviders(@PageableDefault(size = 10) Pageable paging,
			@RequestParam(name = "columns", required = true) List<String> columns) throws NoSuchFieldException {
		assertSaleDepartment();

		List<Map<String, Object>> rows = crudService.read(Provider.class, columns, paging);

		return send(rows, null);
	}

	@GetMapping("/count")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvidersCount() {
		assertSaleDepartment();

		return makeStaleWhileRevalidate(baseRepository.count(Provider.class), COMMON_CACHE_MAXAGE, TimeUnit.DAYS, 3,
				TimeUnit.DAYS);
	}

}
