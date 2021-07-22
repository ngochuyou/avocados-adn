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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import adn.application.Constants;
import adn.model.entities.Entity;
import adn.model.entities.Provider;

/**
 * Basic controller for entities whose business is fairly simple and general
 * 
 * @author Ngoc Huy
 *
 */
@Controller
public class GeneralController extends BaseController {

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
			@PageableDefault(size = 10) Pageable paging,
			@RequestParam(name = "columns", defaultValue = "") List<String> columns,
			@RequestParam(name = "groupby", defaultValue = "") List<String> groupByColumns) {
	// @formatter:on
		try {
			String className = Character.toUpperCase(entityName.charAt(0)) + entityName.substring(1);
			Class<? extends Entity> type = (Class<? extends Entity>) Class
					.forName(Constants.ENTITY_PACKAGE + "." + className);

			return ResponseEntity.ok(crudService.read(type, from(columns), paging, from(groupByColumns)));
		} catch (ClassNotFoundException e) {
			return sendNotFound(String.format("Resource %s not found", entityName));
		} catch (SQLSyntaxErrorException e) {
			return sendBadRequest(e.getMessage());
		}
	}

}
