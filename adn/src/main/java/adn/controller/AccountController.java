package adn.controller;

import javax.servlet.http.HttpServletResponse;

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

import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Account;
import adn.model.factory.extraction.AccountRoleExtractor;
import adn.model.models.AccountModel;
import adn.service.Role;
import adn.service.Service.Status;
import adn.service.ServiceResult;
import adn.service.resource.ResourceManager;
import adn.service.services.AccountService;
import adn.service.services.ResourceService;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	protected final AccountService accountService;
	protected final AccountRoleExtractor roleExtractor;

	protected final ResourceService resourceService;
	protected final ResourceManager resourceManager;

	protected final static String MISSING_ROLE = "USER ROLE IS MISSING";
	protected final static String NOT_FOUND = "USER NOT FOUND";

	// @formatter:off
	@Autowired
	public AccountController(
			final AccountService accountService,
			final AccountRoleExtractor roleExtractor,
			final ResourceService resourceService,
			final ResourceManager resourceManager) {
		this.accountService = accountService;
		this.roleExtractor = roleExtractor;
		this.resourceService = resourceService;
		this.resourceManager = resourceManager;
	}
	// @formatter:on

	@SuppressWarnings("unchecked")
	@Transactional
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> createAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo, HttpServletResponse response)
			throws Exception {
		Role modelRole = roleExtractor.extractRole(jsonPart);

		if (modelRole == null) {
			return ResponseEntity.badRequest().body(MISSING_ROLE);
		}

		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelsDescriptor
				.getModelClass(entityClass);
		AccountModel model;

		try {
			model = mapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException any) {
			any.printStackTrace();
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ACCESS_DENIED);
		}

		openSession();

		if (dao.findById(model.getId(), Account.class) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(EXISTED);
		}

		Account account = extract(model, entityClass);
		ServiceResult<String> uploadResult = resourceService.uploadImage(photo);

		if (!uploadResult.isOk()) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UPLOAD_FAILURE);
		}

		account.setPhoto(uploadResult.getBody());
		DatabaseInteractionResult<? extends Account> insertResult = dao.insert(TypeHelper.unwrap(account), entityClass);

		if (insertResult.isOk()) {
			resourceManager.flush();
			currentSession(ss -> ss.flush());

			return ResponseEntity.ok(produce(account, modelClass));
		}

		resourceManager.clear();
		currentSession(ss -> ss.clear());

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
				return resourceService.getImageBytes(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
			}

			username = authentication.getName();
		}

		Account account = dao.findById(username, Account.class);

		if (account == null) {
			return resourceService.getImageBytes(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
		}

		return resourceService.getImageBytes(account.getPhoto());
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> updateAccount(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile multipartPhoto) throws Exception {
		Role modelRole = roleExtractor.extractRole(jsonPart);

		if (modelRole == null) {
			return ResponseEntity.badRequest().body(MISSING_ROLE);
		}

		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelsDescriptor
				.getModelClass(entityClass);
		AccountModel model;

		try {
			model = mapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		String principalName = ContextProvider.getPrincipalName();
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ACCESS_DENIED);
		}
		// get current session with FlushMode.MANUAL
		openSession();

		Account persistence;
		// This entity will take effects as the handler progresses
		// Only changes on this persisted entity will be committed
		if ((persistence = dao.findById(model.getUsername(), Account.class)) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(NOT_FOUND);
		}
		// Extract the model entity from Model and make adjustments
		// Do not persist this entity
		Account account = extract(model, entityClass);
		// only account's owner can update password
		if (!account.getId().equals(principalName) || !StringHelper.hasLength(account.getPassword())) {
			account.setPassword(null);
		}

		ServiceResult<String> result = updateOrUploadPhoto(persistence, multipartPhoto);

		if (!result.isOk()) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UPLOAD_FAILURE);
		}

		DatabaseInteractionResult<? extends Account> updateResult = dao.update(TypeHelper.unwrap(account), entityClass);

		if (updateResult.isOk()) {
			resourceManager.flush();
			currentSession(ss -> ss.flush());

			return ResponseEntity.ok(produce(persistence, modelClass));
		}

		resourceManager.clear();
		currentSession(ss -> ss.clear());

		return ResponseEntity.status(updateResult.getStatus()).body(updateResult.getMessages());
	}

	private ServiceResult<String> updateOrUploadPhoto(Account persistence, MultipartFile multipartPhoto) {
		if (multipartPhoto != null) {
			if (!persistence.getPhoto().equals(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME)) {
				ServiceResult<String> result = resourceService.updateContent(multipartPhoto, persistence.getPhoto());

				if (!result.isOk()) {
					return ServiceResult.status(Status.FAILED);
				}

				return ServiceResult.ok(result.getBody());
			}
		}

		return ServiceResult.ok(persistence.getPhoto());
	}

}
