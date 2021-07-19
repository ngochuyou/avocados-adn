/**
 * 
 */
package adn.controller;

import static adn.helpers.ArrayHelper.from;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/provider")
public class ProviderController extends BaseController {

	private static final int COMMON_CACHE_MAXAGE = 2;

	@GetMapping
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getAllProviders(@PageableDefault(size = 10) Pageable paging,
			@RequestParam(name = "columns", required = true) List<String> columns) {
		try {
			List<Map<String, Object>> rows = crudService.read(Provider.class, from(columns), paging);

			return cache(rows, COMMON_CACHE_MAXAGE, TimeUnit.DAYS);
		} catch (SQLSyntaxErrorException ssee) {
			return sendBadRequest(ssee.getMessage());
		}
	}

	@GetMapping("/count")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvidersCount() {
		return cache(baseRepository.count(Provider.class), COMMON_CACHE_MAXAGE, TimeUnit.DAYS);
	}

}
