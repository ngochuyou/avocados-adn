/**
 * 
 */
package adn.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import adn.application.context.ContextProvider;
import adn.model.entities.District;
import adn.model.entities.Province;
import adn.model.entities.metadata._District;
import adn.model.entities.metadata._Entity;
import adn.model.factory.authentication.dynamicmap.UnauthorizedCredential;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/admindivision")
public class AdministrativeDivisionController extends BaseController {

	@GetMapping("/province")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getProvincesList(@RequestParam(required = false, defaultValue = "") List<String> columns,
			@PageableDefault Pageable paging) throws NoSuchFieldException, UnauthorizedCredential {
		return makeStaleWhileRevalidate(
				crudService.readAll(Province.class, columns, paging, ContextProvider.getPrincipalCredential()), 365,
				TimeUnit.DAYS, 365 * 2, TimeUnit.DAYS);
	}

	@GetMapping("/district/{provinceId}")
	@Transactional(readOnly = true)
	public ResponseEntity<?> getDistrictsListByProvince(@PathVariable(name = "provinceId") Integer provinceId,
			@RequestParam(required = false, defaultValue = "") List<String> columns, @PageableDefault Pageable paging)
			throws NoSuchFieldException, Throwable {
		return makeStaleWhileRevalidate(
				crudService.readAll(District.class, columns,
						(root, query, builder) -> builder.equal(root.get(_District.province).get(_Entity.id),
								provinceId),
						paging, ContextProvider.getPrincipalCredential()),
				365, TimeUnit.DAYS, 365 * 2, TimeUnit.DAYS);
	}

}
