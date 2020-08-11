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
	
	@PreAuthorize(HASROLE_ADMIN)
	@PostMapping("/admin")
	public ResponseEntity<?> registerAdmin(@RequestBody AdminModel model) {
		System.out.println("you've reached admin creation api");
		
		return null;
	}
	
}
