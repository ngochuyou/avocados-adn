package adn.controller;

import static adn.helpers.HibernateHelper.useManualSession;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.springframework.http.ResponseEntity.ok;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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

import adn.application.Common;
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.application.context.builders.CredentialFactory;
import adn.helpers.StringHelper;
import adn.model.entities.User;
import adn.service.UserRoleExtractor;
import adn.service.internal.ResourceService;
import adn.service.internal.Role;
import adn.service.services.AuthenticationService;
import adn.service.services.UserService;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController {

	protected final UserService accountService;
	protected final ResourceService resourceService;
	protected final AuthenticationService authService;

	protected final UserRoleExtractor roleExtractor;

	protected final static String MISSING_ROLE = "USER ROLE IS MISSING";
	protected final static int PHOTO_CACHE_CONTROL_MAX_AGE = 3; // days
	// @formatter:off
	@Autowired
	public UserController(
			final UserService accountService,
			final UserRoleExtractor roleExtractor,
			final ResourceService resourceService, AuthenticationService authService) {
		this.accountService = accountService;
		this.authService = authService;
		this.roleExtractor = roleExtractor;
		this.resourceService = resourceService;
	}
	// @formatter:on

	@SuppressWarnings("unchecked")
	@Transactional
	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> createUser(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile photo, HttpServletResponse response)
			throws Exception {
		Role modelRole = roleExtractor.extractRole(jsonPart);

		if (modelRole == null) {
			return bad(Common.message(MISSING_ROLE));
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return bad(Common.message(Common.ACCESS_DENIED));
		}

		Class<? extends User> accountClass = accountService.getClassFromRole(modelRole);
		User model;

		try {
			model = objectMapper.readValue(jsonPart, accountClass);
		} catch (JsonProcessingException any) {
			return bad(Common.INVALID_MODEL);
		}

		useManualSession();

		if (genericRepository.countById(User.class, model.getId()) != 0) {
			return conflict(Common.existed());
		}

		Result<User> insertResult = accountService.create(model.getId(), model, (Class<User>) accountClass, photo,
				true);

		if (insertResult.isOk()) {
			if (modelRole == Role.CUSTOMER) {
				return ResponseEntity
						.ok(produce(insertResult.getInstance(), (Class<User>) accountClass, CredentialFactory.owner()));
			}

			return ok(produce(insertResult.getInstance(), (Class<User>) accountClass, principalRole));
		}

		return bad(insertResult.getMessages());
	}

	@Transactional(readOnly = true)
	@GetMapping("/photo")
	public @ResponseBody Object obtainPhotoBytes(
			@RequestParam(name = "username", required = false, defaultValue = "") String username,
			@RequestParam(name = "filename", required = false) final String filename, Authentication authentication)
			throws Exception {
		if (filename != null) {
			return cacheUserPhoto(resourceService.directlyGetUserPhotoBytes(filename));
		}

		if (!StringHelper.hasLength(username)) {
			if (authentication == null) {
				return notFound();
			}

			username = authentication.getName();
		}

		Optional<User> optional = genericRepository.findById(User.class, username);

		if (optional.isEmpty()) {
			return notFound();
		}

		return cacheUserPhoto(resourceService.directlyGetImageBytes(null, optional.get().getPhoto()));
	}

	private ResponseEntity<?> cacheUserPhoto(byte[] photoBytes) {
		return makeStaleWhileRevalidate(photoBytes, PHOTO_CACHE_CONTROL_MAX_AGE, DAYS, 7, DAYS);
	}

	@Transactional
	@SuppressWarnings("unchecked")
	@PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public @ResponseBody ResponseEntity<?> updateUser(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "photo", required = false) MultipartFile multipartPhoto) throws Exception {
		Role modelRole = roleExtractor.extractRole(jsonPart);

		if (modelRole == null) {
			return ResponseEntity.badRequest().body(MISSING_ROLE);
		}

		Class<? extends User> accountClass = accountService.getClassFromRole(modelRole);
		User model;

		try {
			model = objectMapper.readValue(jsonPart, accountClass);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return bad(Common.INVALID_MODEL);
		}

		Role principalRole = ContextProvider.getPrincipalRole();

		if (!principalRole.canModify(modelRole)) {
			return unauthorize(Common.ACCESS_DENIED);
		}
		// get current session with FlushMode.MANUAL
		useManualSession();

		User persistence;
		// This entity will take effects as the handler progresses
		// Only changes on this persisted entity will be committed
		if ((persistence = genericRepository.findById(User.class, model.getId()).orElse(null)) == null) {
			return notFound();
		}

		Result<User> updateResult = accountService.update(persistence.getId(), model, (Class<User>) accountClass,
				multipartPhoto, true);

		if (updateResult.isOk()) {
			return ResponseEntity.ok(produce(updateResult.getInstance(), (Class<User>) accountClass, principalRole));
		}

		return bad(updateResult.getStatus());
	}

}
