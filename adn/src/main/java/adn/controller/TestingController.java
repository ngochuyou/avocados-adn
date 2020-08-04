/**
 * 
 */
package adn.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/t")
public class TestingController extends BaseController {

	@GetMapping("/greet")
	public ResponseEntity<?> greet() {

		return handleSuccess("Hello");
	}

}
