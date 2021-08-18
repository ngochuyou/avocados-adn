package adn.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
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
import adn.dao.generic.Result;
import adn.helpers.StringHelper;
import adn.model.entities.Account;
import adn.service.AccountRoleExtractor;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.services.AccountService;

@Controller
@RequestMapping("/account")
public class AccountController extends BaseController {

	protected final AccountService accountService;
	protected final AccountRoleExtractor roleExtractor;

	protected final ResourceService resourceService;

	protected final static String MISSING_ROLE = "USER ROLE IS MISSING";
	protected final static String NOT_FOUND = "USER NOT FOUND";
	protected final static int PHOTO_CACHE_CONTROL_MAX_AGE = 3; // days
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
			@RequestPart(name = "photo", required = false) MultipartFile photo) throws Exception {
		Role modelRole = roleExtractor.extractRole(jsonPart);

		if (modelRole == null) {
			return ResponseEntity.badRequest().body(MISSING_ROLE);
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return unauthorize(ACCESS_DENIED);
		}

		Class<? extends Account> accountClass = accountService.getClassFromRole(modelRole);
		Account model;

		try {
			model = objectMapper.readValue(jsonPart, accountClass);
		} catch (JsonProcessingException any) {
			any.printStackTrace();
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		setSessionMode();

		if (baseRepository.countById(model.getId(), Account.class) != 0) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(EXISTED);
		}

		Result<Account> insertResult = accountService.create(model.getId(), model,
				(Class<Account>) accountClass, photo, true);

		if (insertResult.isOk()) {
			return ResponseEntity.ok(produce(insertResult.getInstance(), (Class<Account>) accountClass, principalRole));
		}

		return sendBadRequest(insertResult.getMessages());
	}

	@Transactional(readOnly = true)
	@GetMapping("/photo")
	public @ResponseBody Object obtainPhotoBytes(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "filename", required = false) final String filename, Authentication authentication)
			throws Exception {
		if (filename != null) {
			return cacheAccountPhoto(resourceService.directlyGetUserPhotoBytes(filename));
		}

		if (!StringHelper.hasLength(username)) {
			if (authentication == null) {
				return sendNotFound(NOT_FOUND);
			}

			username = authentication.getName();
		}

		Account account = baseRepository.findById(username, Account.class);

		if (account == null) {
			return sendNotFound(NOT_FOUND);
		}

		return cacheAccountPhoto(resourceService.directlyGetImageBytes(null, account.getPhoto()));
	}

	private ResponseEntity<?> cacheAccountPhoto(byte[] photoBytes) {
		// @formatter:off
		return ResponseEntity.ok()
				.cacheControl(CacheControl.maxAge(PHOTO_CACHE_CONTROL_MAX_AGE, TimeUnit.DAYS))
				.body(photoBytes);
		// @formatter:on
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

		Class<? extends Account> accountClass = accountService.getClassFromRole(modelRole);
		Account model;

		try {
			model = objectMapper.readValue(jsonPart, accountClass);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return unauthorize(ACCESS_DENIED);
		}
		// get current session with FlushMode.MANUAL
		setSessionMode();

		Account persistence;
		// This entity will take effects as the handler progresses
		// Only changes on this persisted entity will be committed
		if ((persistence = baseRepository.findById(model.getId(), Account.class)) == null) {
			return sendNotFound(NOT_FOUND);
		}

		Result<Account> updateResult = accountService.update(persistence.getId(), model,
				(Class<Account>) accountClass, multipartPhoto, true);

		if (updateResult.isOk()) {
			return ResponseEntity.ok(produce(updateResult.getInstance(), (Class<Account>) accountClass, principalRole));
		}

		return sendBadRequest(updateResult.getStatus());
	}

}
