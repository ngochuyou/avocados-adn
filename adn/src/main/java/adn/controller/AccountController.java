package adn.controller;

import static adn.service.services.AccountService.DEFAULT_ACCOUNT_PHOTO_NAME;

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
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.internal.ServiceResult;
import adn.service.services.AccountService;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	protected final AccountService accountService;
	protected final AccountRoleExtractor roleExtractor;

	protected final ResourceService resourceService;

	protected final static String MISSING_ROLE = "USER ROLE IS MISSING";
	protected final static String NOT_FOUND = "USER NOT FOUND";

	// @formatter:off
	@Autowired
	public AccountController(
			final AccountService accountService,
			final AccountRoleExtractor roleExtractor,
			final ResourceService resourceService) {
		this.accountService = accountService;
		this.roleExtractor = roleExtractor;
		this.resourceService = resourceService;
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

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return unauthorize(ACCESS_DENIED);
		}

		Class<? extends Account> entityClass = accountService.getClassFromRole(modelRole);
		Class<? extends AccountModel> modelClass = (Class<? extends AccountModel>) modelsDescriptor
				.getModelClass(entityClass);
		AccountModel model;

		try {
			model = objectMapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException any) {
			any.printStackTrace();
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		openSession();

		if (baseRepository.<Account>findById(model.getId(), Account.class) != null) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(EXISTED);
		}

		Account account = extract(model, entityClass);

		if (photo != null) {
			ServiceResult<String> uploadResult = resourceService.uploadImage(photo);

			if (!uploadResult.isOk()) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UPLOAD_FAILURE);
			}

			account.setPhoto(uploadResult.getBody());
		}

		DatabaseInteractionResult<? extends Account> insertResult = crudService.create(account.getId(),
				TypeHelper.unwrap(account), entityClass);

		resourceService.closeSession(insertResult.isOk());

		if (insertResult.isOk()) {
			currentSession(ss -> ss.flush());

			return ResponseEntity.ok(produce(insertResult.getInstance(), modelClass));
		}

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
				return resourceService.directlyGetImageBytes(DEFAULT_ACCOUNT_PHOTO_NAME);
			}

			username = authentication.getName();
		}

		Account account = baseRepository.findById(username, Account.class);

		if (account == null) {
			return resourceService.directlyGetImageBytes(DEFAULT_ACCOUNT_PHOTO_NAME);
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
			model = objectMapper.readValue(jsonPart, modelClass);
		} catch (JsonProcessingException e) {
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		String principalName = ContextProvider.getPrincipalName();
		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return unauthorize(ACCESS_DENIED);
		}
		// get current session with FlushMode.MANUAL
		openSession();

		Account persistence;
		// This entity will take effects as the handler progresses
		// Only changes on this persisted entity will be committed
		if ((persistence = baseRepository.findById(model.getUsername(), Account.class)) == null) {
			return sendNotFound(NOT_FOUND);
		}
		// Extract the entity from Model and make adjustments
		// Do not persist this entity
		Account account = extract(model, entityClass);

		if (!persistence.getRole().equals(account.getRole())) {
			// determine role update, current only administrators could update an account's
			// role
			if (!principalRole.equals(Role.ADMIN)) {
				return unauthorize(ACCESS_DENIED);
			}

			if (!persistence.getRole().canBeUpdatedTo(account.getRole())) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
						String.format("Unable to update role from %s to %s", persistence.getRole(), account.getRole()));
			}
		}
		// only account's owner can update password
		if (!account.getId().equals(principalName)) {
			account.setPassword(null);
		}

		ServiceResult<String> result = updateOrUploadPhoto(persistence, multipartPhoto);

		if (!result.isOk()) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UPLOAD_FAILURE);
		}
		// set photo upload result into account so that CRUDService inject it into the
		// persistence instead of directly setting it into persistence since CRUDService
		// will inject account#getPhoto(), which is null if we set upload result
		// directly into persistence here
		account.setPhoto(result.getBody());
		// changes made within following operations take effect on the persistence
		// reference
		DatabaseInteractionResult<? extends Account> updateResult = crudService.update(account.getId(),
				TypeHelper.unwrap(account), entityClass);

		resourceService.closeSession(updateResult.isOk());

		if (updateResult.isOk()) {
			currentSession(ss -> ss.flush());

			return ResponseEntity.ok(produce(persistence, modelClass));
		}

		currentSession(ss -> ss.clear());

		return ResponseEntity.status(updateResult.getStatus()).body(updateResult.getMessages());
	}

	private ServiceResult<String> updateOrUploadPhoto(Account persistence, MultipartFile multipartPhoto) {
		if (multipartPhoto != null) {
			if (!persistence.getPhoto().equals(DEFAULT_ACCOUNT_PHOTO_NAME)) {
				return resourceService.updateContent(multipartPhoto, persistence.getPhoto());
			}

			return resourceService.uploadImage(multipartPhoto);
		}

		return ServiceResult.ok(persistence.getPhoto());
	}

}
