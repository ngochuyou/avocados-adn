package adn.controller;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.FlushMode;
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
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Account;
import adn.model.factory.extraction.AccountRoleExtractor;
import adn.model.models.AccountModel;
import adn.service.ServiceResult;
import adn.service.resource.ResourceManager;
import adn.service.services.AccountService;
import adn.service.services.ResourceService;
import adn.service.services.Role;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	@Autowired
	protected AccountService accountService;

	@Autowired
	protected ResourceService resourceService;

	@Autowired
	protected ResourceManager resourceManager;

	@Autowired
	protected AccountRoleExtractor roleExtractor;

	protected final String missingRole = "USER ROLE IS MISSING";
	protected final String notFound = "USER NOT FOUND";

	@SuppressWarnings("unchecked")
	@Transactional
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> createAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo, HttpServletResponse response) {
		Role modelRole = roleExtractor.extractRole(jsonPart);

		if (modelRole == null) {
			return ResponseEntity.badRequest().body(missingRole);
		}

		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelsDescriptor
				.getModelClass(entityClass);
		AccountModel model;

		try {
			model = mapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().body(invalidModel);
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(accessDenied);
		}

		if (dao.findById(model.getId(), Account.class) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(existed);
		}

		Account account = extract(model, entityClass);
		ServiceResult<String> uploadResult = resourceService.uploadImage(photo);

		if (!uploadResult.isOk()) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uploadFailure);
		}

		account.setPhoto(uploadResult.getBody());
		DatabaseInteractionResult<? extends Account> insertResult = dao.insert(TypeHelper.unwrap(account), entityClass);

		if (insertResult.isOk()) {
			resourceManager.flush();
			sessionFactory.getCurrentSession().flush();

			return ResponseEntity.ok(produce(account, modelClass));
		}

		resourceManager.clear();
		sessionFactory.getCurrentSession().clear();

		return ResponseEntity.status(insertResult.getStatus()).body(insertResult.getMessages());
	}

	@Transactional(readOnly = true)
	@GetMapping("/photo")
	public @ResponseBody Object obtainPhotoBytes(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "filename", required = false) final String filename, Authentication authentication)
			throws Exception {
		if (filename != null) {
			return resourceService.getImageBytes(filename);
		}

		if (!StringHelper.hasLength(username)) {
			if (authentication == null) {
				return resourceService.getImageBytes(Constants.DEFAULT_USER_PHOTO_NAME);
			}

			username = authentication.getName();
		}

		Account account = dao.findById(username, Account.class);

		if (account == null) {
			return resourceService.getImageBytes(Constants.DEFAULT_USER_PHOTO_NAME);
		}

		return resourceService.getImageBytes(account.getPhoto());
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> updateAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo)
			throws NoSuchMethodException, SecurityException {
		Role modelRole = roleExtractor.extractRole(jsonPart);
		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelsDescriptor
				.getModelClass(entityClass);
		AccountModel model;

		try {
			model = mapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().body(invalidModel);
		}

		String principalName = ContextProvider.getPrincipalName();
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(accessDenied);
		}
		// get current session with FlushMode.MANUAL
		openSession(FlushMode.MANUAL);
		// This entity will take effects as the handler progresses
		// Only changes on this persisted entity will be committed
		if (dao.findById(model.getUsername(), Account.class) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(notFound);
		}
		// Extract the model entity from Model and make adjustments
		// Do not persist this entity
		Account account = extract(model, entityClass);
		// only account's owner can update password
		if (!account.getId().equals(principalName) || !StringHelper.hasLength(account.getPassword())) {
			account.setPassword(null);
		}

		if (photo != null) {
			if (!resourceService.updateContent(photo, account.getPhoto()).isOk()) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(uploadFailure);
			}
		}

		DatabaseInteractionResult<? extends Account> updateResult = dao.update(TypeHelper.unwrap(account), entityClass);

		closeSession(updateResult.isOk());

		if (updateResult.isOk()) {
			return ResponseEntity.ok(produce(dao.findById(account.getId(), Account.class), modelClass));
		}

		return ResponseEntity.status(updateResult.getStatus()).body(updateResult.getMessages());
	}

}
