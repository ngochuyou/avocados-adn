package adn.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.util.Strings;
import org.hibernate.FlushMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import adn.service.ServiceResult;
import adn.service.services.AccountService;
import adn.service.services.FileService;
import adn.service.transaction.Event;
import adn.service.transaction.GlobalTransaction;
import adn.service.transaction.TransactionException;
import adn.utilities.Role;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	@Autowired
	protected AccountService accountService;

	@Autowired
	protected FileService fileService;

	protected final String missingRole = "USER ROLE IS MISSING";

	protected final String roleFieldname = "role";

	protected final String notFound = "USER NOT FOUND";

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@SuppressWarnings("unchecked")
	@Transactional
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

		if (dao.findById(model.getId(), Account.class) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(exsited);
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (model.getRole() != null && model.getRole().equals(Role.ADMIN.name()) && !principalRole.equals(Role.ADMIN)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessDenied);
		}

		Account account = extract(model, entityClass);
		Result<? extends Account> insertionResult = dao.insert(reflector.genericallyCast(account), entityClass);

		if (insertionResult.isOk()) {
			sessionFactory.getCurrentSession().flush();

			return ResponseEntity.ok(produce(insertionResult.getInstance(), modelClass));
		}

		sessionFactory.getCurrentSession().clear();

		return ResponseEntity.status(insertionResult.getStatus()).body(insertionResult.getMessageSet());
	}

	@Transactional(readOnly = true)
	@GetMapping("/photo")
	public @ResponseBody Object obtainPhoto(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "filename", required = false) String filename, Authentication authentication)
			throws Exception {
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

	@Transactional
	@SuppressWarnings("unchecked")
	@PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> updateAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo)
			throws NoSuchMethodException, SecurityException, TransactionException {
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
		// get current session with FlushMode.MANUAL
		openSession(FlushMode.MANUAL);
		// This entity will takes effects throughout as the handler progress
		// Only changes on this persisted entity will be committed
		if (dao.findById(model.getUsername(), Account.class) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
		}
		// Extract the model entity from Model and make adjustments
		// Data from this object will be updated to the persisted entity
		Account account = extract(model, entityClass);
		// only account's owner can update password
		if (!(account.getId().equals(principalName) && !Strings.isEmpty(account.getPassword()))) {
			account.setPassword(null);
		}
		// only administrators can update role
		if (!principalRole.equals(Role.ADMIN)) {
			account.setRole(null);
		}
		// open a global transaction for image upload
		GlobalTransaction transaction = globalTransactionManager.openTransaction();

		if (photo != null) {
			ServiceResult<String> uploadResult = fileService.uploadFile(photo, transaction);
			// register rollback method
			logger.debug("Registering rollback FileService.removeFile to transaction. Transaction id: "
					+ transaction.getId());
			transaction.addRollback(Event.functional(fileService::removeFile, uploadResult.getBody(), "removeFile"));
			// set output into persisted entity
			account.setPhoto(uploadResult.getBody());
		}

		Result<? extends Account> result = dao.update(reflector.genericallyCast(account), entityClass, Account.class);

		if (result.isOk()) {
			// commit and close transaction
			transaction.commit();
			globalTransactionManager.closeTransaction(transaction.getId());
		}
		// if the transaction successfully executed, flush the Hibernate session
		closeSession(result.isOk());

		if (result.isOk()) {
			return ResponseEntity.ok(produce(result.getInstance(), modelClass));
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
