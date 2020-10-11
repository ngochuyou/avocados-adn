package adn.controller;

import javax.servlet.http.HttpServletResponse;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.dao.generic.EntityGeneBuilder;
import adn.model.Result;
import adn.model.entities.Account;
import adn.model.models.AccountModel;
import adn.service.ServiceResult;
import adn.service.services.AccountService;
import adn.service.services.FileService;
import adn.service.transaction.Mode;
import adn.service.transaction.Event;
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
			@RequestPart(name = "photo", required = false) MultipartFile photo, HttpServletResponse response) {
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

//		transaction.setStrategy(Strategy.TRANSACTIONAL);
		account = geneBuilder.insert().build(account);

		if (photo != null) {
			ServiceResult uploadResult = fileService.uploadFile(photo);

			if (!uploadResult.isOk()) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uploadResult.getBody());
			}

			Event event = (Event) uploadResult.getBody();

			account.setPhoto(event.getOutput().toString());
		}

		Result<? extends Account> insertionResult = dao.insert(reflector.genericallyCast(account), entityClass);

		sessionFactory.getCurrentSession().clear();

		if (insertionResult.isOk()) {
			setFlushMode(response, Mode.COMMIT_ALL);

			return ResponseEntity.ok(produce(insertionResult.getInstance(), modelClass));
		}

		setFlushMode(response, Mode.CLEAR_ALL);

		return ResponseEntity.status(insertionResult.getStatus()).body(insertionResult.getMessageSet());
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

//	@SuppressWarnings("unchecked")
//	@PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
//	public @ResponseBody ResponseEntity<?> updateAccount(@RequestPart(name = "model", required = true) String jsonPart,
//			@RequestPart(name = "photo", required = false) MultipartFile photo) {
//		Role modelRole = getRoleFromJsonString(jsonPart);
//		AccountModel model;
//		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
//		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelManager
//				.getModelClass(entityClass);
//
//		try {
//			model = mapper.readValue(jsonPart, modelClass);
//		} catch (JsonProcessingException e) {
//			return ResponseEntity.badRequest().body(invalidModel);
//		}
//
//		String principalName = ContextProvider.getPrincipalName();
//		Role principalRole = ContextProvider.getPrincipalRole();
//
//		if (!model.getUsername().equals(principalName) && !principalRole.equals(Role.ADMIN)) {
//			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessDenied);
//		}
//
//		Account persistedAccount = dao.findById(model.getUsername(), Account.class);
//
//		if (persistedAccount == null) {
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
//		}
//
//		Account account;
//		EntityGeneBuilder<Account> geneBuilder = new EntityGeneBuilder<>(entityClass);
//
//		account = extract(model, entityClass);
//		persistedAccount = geneBuilder.update().build(account);
//
//		String oldPhotoName = persistedAccount.getPhoto();
//
//		if (photo != null) {
//			persistedAccount.setPhoto(fileService.generateFilename(photo));
//		}
//
//		if ((account.getId().equals(principalName) || principalRole.equals(Role.ADMIN))
//				&& !Strings.isEmpty(account.getPassword())) {
//			persistedAccount.setPassword(passwordEncoder.encode(account.getPassword()));
//		}
//
//		if (principalRole.equals(Role.ADMIN)) {
//			persistedAccount.setRole(account.getRole());
//		}
//
//		Result<? extends Account> result = dao.update(reflector.genericallyCast(persistedAccount), Account.class);
//
//		boolean isOk = result.isOk();
//
//		if (isOk) {
//			ServiceResult uploadResult = fileService.uploadFile(photo, persistedAccount.getPhoto());
//
//			if (uploadResult.isOk()) {
//				ServiceResult removalResult = fileService.removeFile(oldPhotoName);
//
//				isOk = isOk && uploadResult.isOk() && removalResult.isOk();
//			}
//		}
//
//		if (isOk) {
//			return ResponseEntity.ok(produce(result.getInstance(), modelClass));
//		}
//
//		return ResponseEntity.status(result.getStatus()).body(result.getMessageSet());
//	}

	protected Role getRoleFromJsonString(String jsonPart) {
		try {
			return Role.valueOf(mapper.readTree(jsonPart).get(roleFieldname).asText());
		} catch (Exception e) {
			return Role.ANONYMOUS;
		}
	}

}
