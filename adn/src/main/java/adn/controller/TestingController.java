/**
 * 
 */
package adn.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
	public ResponseEntity<?> greet(@CookieValue(name = "_sie9t", required = false) Cookie c,
			HttpServletResponse response) {
		System.out.println(c == null);

		response.setHeader("Access-Control-Allow-Credentials", "true");

		return handleSuccess("Hello");
	}

}
