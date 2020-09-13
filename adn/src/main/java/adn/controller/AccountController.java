package adn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.ContextProvider;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.service.generic.AccountService;
import adn.utilities.Role;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	@Autowired
	protected AccountService accountService;

	@Autowired
	protected ObjectMapper mapper;

	protected static final String missingRole = "ACCOUNT ROLE IS MISSING";

	@SuppressWarnings("unchecked")
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> createAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile file) {
		Role modelRole = getRoleFromJsonString(jsonPart);

		if (modelRole == null) {
			return ResponseEntity.badRequest().body(missingRole);
		}

		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelManager
				.getModelClass(entityClass);
		AccountModel model;

		try {
			model = mapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().body(invalidModel);
		}

		Role principalRole = ContextProvider.getPrincipalRole();
		
		if (model.getRole().equals(Role.ADMIN.name()) && !principalRole.equals(principalRole)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("ACCESS DENIED");
		}
		
		Account account = authBasedEMFactory.produce(model, entityClass);

		return null;
	}

	protected Role getRoleFromJsonString(String jsonPart) {
		try {
			return Role.valueOf(mapper.readTree(jsonPart).get("role").asText());
		} catch (Exception e) {
			return null;
		}
	}

}
