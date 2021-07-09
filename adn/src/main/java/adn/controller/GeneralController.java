/**
 * 
 */
package adn.controller;

import static adn.helpers.ArrayHelper.from;

import java.sql.SQLSyntaxErrorException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import adn.application.Constants;
import adn.model.entities.Factor;
import adn.model.entities.Provider;

/**
 * Basic controller for entities whose business is fairly simple and general
 * 
 * @author Ngoc Huy
 *
 */
@Controller
public class GeneralController extends BaseController {

	@Transactional
	@PostMapping("/provider")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> createProvider(@RequestBody Provider provider) {
		return createFactor(provider, Provider.class);
	}

	@Transactional
	@PutMapping("/provider")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> updateProvider(@RequestBody Provider provider) {
		return updateFactor(provider, Provider.class);
	}

	@Transactional(readOnly = true)
	@GetMapping("/provider")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> obtainProvider(@RequestParam(name = "id") UUID id) {
		return super.<Provider>send(baseRepository.findById(id, Provider.class),
				String.format("Provider with id %s not found", id));
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@GetMapping("/list/{entityname}")
	@Secured({ "ROLE_ADMIN", "ROLE_PERSONNEL" })
	public @ResponseBody ResponseEntity<?> obtainFactor(
	// @formatter:off
			@PathVariable(name = "entityname") String entityName,
			@PageableDefault(size = 20) Pageable paging,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns,
			@RequestParam(name = "groupby", defaultValue = "") List<String> groupByColumns) {
	// @formatter:on
		try {
			String className = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);
			Class<? extends Factor> type = (Class<? extends Factor>) Class
					.forName(Constants.ENTITY_PACKAGE + "." + className);

			return ResponseEntity.ok(crudService.read(type, from(columns), paging, from(groupByColumns)));
		} catch (ClassNotFoundException e) {
			return sendNotFound(String.format("Resource %s not found", entityName));
		} catch (SQLSyntaxErrorException e) {
			return sendBadRequest(e.getMessage());
		}
	}

	/**
	 * @param instance always non-null
	 * @param type     always non-null
	 */
	protected <T extends Factor> ResponseEntity<?> createFactor(T instance, Class<T> type) {
		// there is a chance where id field in the model is provided. In such case, we
		// ignore it since it could cause the Specification check on the name uniqueness
		// to success. This results in Session being flushed upon a duplicated name,
		// which causes violation exception
		setSessionMode();

		return send(crudService.create(null, instance, type));
	}

	/**
	 * @param instance always non-null
	 * @param type     always non-null
	 */
	protected <T extends Factor> ResponseEntity<?> updateFactor(T instance, Class<T> type) {
		setSessionMode();
		// load the actual factor into Session, hit the DB
		if (baseRepository.findById(instance.getId(), type) == null) {
			return sendNotFound(String.format("%s not found", instance.getId()));
		}

		return send(crudService.update(instance.getId(), instance, type));
	}

}
