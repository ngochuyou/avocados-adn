/**
 * 
 */
package adn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adn.model.models.AdminModel;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/rest/account")
public class RestAccountController extends BaseController {

	@PreAuthorize(hasRoleAdmin)
	@PostMapping("/admin")
	public ResponseEntity<?> registerAdmin(@RequestBody AdminModel model) {

		return ResponseEntity.ok(null);
	}

}
