package adn.controller;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.model.Result;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.service.ApplicationService.ServiceStatus;
import adn.service.EntityGeneBuilder;
import adn.service.ServiceResult;
import adn.service.generic.AccountService;
import adn.service.services.FileService;
import adn.utilities.Role;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	@Autowired
	protected AccountService accountService;

	@Autowired
	protected FileService fileService;

	@Autowired
	protected PasswordEncoder passwordEncoder;
	
	protected final String missingRole = "USER ROLE IS MISSING";

	protected final String roleFieldname = "role";

	protected final String notFound = "USER NOT FOUND";

	@SuppressWarnings("unchecked")
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> createAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo) {
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

		if (model.getRole() != null && model.getRole().equals(Role.ADMIN.name()) && !principalRole.equals(Role.ADMIN)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessDenied);
		}

		Account account = extract(model, entityClass);
		EntityGeneBuilder<Account> geneBuilder = new EntityGeneBuilder<>(entityClass);

		account = geneBuilder.insert().build(account);

		if (photo != null) {
			ServiceResult uploadResult = fileService.uploadFile(photo);

			account.setPhoto(uploadResult.getStatus().equals(ServiceStatus.OK) ? uploadResult.getBody().toString()
					: account.getPhoto());
		}

		openSession();

		Result<?> insertResult = dao.insert(reflector.genericallyCast(account), entityClass);

		clearSession(insertResult.isOk());

		if (!insertResult.isOk()) {
			return ResponseEntity.status(insertResult.getStatus()).body(insertResult.getMessageSet());
		}

		return ResponseEntity.ok(produce(account, modelClass));
	}

	@Transactional(readOnly = true)
	@GetMapping("/photo")
	public @ResponseBody Object obtainPhoto(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "filename", required = false) String filename, Authentication authentication) {
		byte[] photoBytes = fileService.getImageBytes(filename);

		if (photoBytes != null) {
			return photoBytes;
		}

		if (Strings.isEmpty(username)) {
			if (authentication == null) {
				return fileService.getImageBytes(Constants.DEFAULT_USER_PHOTO_NAME);
			}

			username = authentication.getName();
		}

		Account account = dao.findById(username, Account.class);

		if (account == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
		}

		return fileService.getImageBytes(account.getPhoto());
	}

	@SuppressWarnings("unchecked")
	@PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<?> updateAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo) {
		Role modelRole = getRoleFromJsonString(jsonPart);
		AccountModel model;
		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelManager
				.getModelClass(entityClass);

		try {
			model = mapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().body(invalidModel);
		}

		String principalName = ContextProvider.getPrincipalName();
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!model.getUsername().equals(principalName) && !principalRole.equals(Role.ADMIN)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessDenied);
		}

		Account persistedAccount = dao.findById(model.getUsername(), Account.class);

		if (persistedAccount == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
		}

		Account account;
		EntityGeneBuilder<Account> geneBuilder = new EntityGeneBuilder<>(entityClass);
		
		account = extract(model, entityClass);
		persistedAccount = geneBuilder.update().build(account);

		if (photo != null) {
			ServiceResult imageUploadResult = fileService.uploadFile(photo);

			if (imageUploadResult.isOk()) {
				ServiceResult imageRemovalResult = fileService.removeFile(persistedAccount.getPhoto());

				persistedAccount.setPhoto(
						imageRemovalResult.isOk() ? imageUploadResult.getBody().toString() : persistedAccount.getPhoto());
			}
		}

		if ((account.getId().equals(principalName) || principalRole.equals(Role.ADMIN)) && !Strings.isEmpty(account.getPassword())) {
			persistedAccount.setPassword(passwordEncoder.encode(account.getPassword()));
		}
		
		if (principalRole.equals(Role.ADMIN)) {
			persistedAccount.setRole(account.getRole());
		}

		Result<?> result = dao.update(reflector.genericallyCast(persistedAccount), Account.class);

		if (result.isOk()) {
			return ResponseEntity.ok(produce(account, modelClass));
		}

		return ResponseEntity.status(result.getStatus()).body(result.getMessageSet());
	}

	protected Role getRoleFromJsonString(String jsonPart) {
		try {
			return Role.valueOf(mapper.readTree(jsonPart).get(roleFieldname).asText());
		} catch (Exception e) {
			return Role.ANONYMOUS;
		}
	}

}
