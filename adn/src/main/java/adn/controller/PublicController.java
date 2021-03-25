/**
 * 
 */
package adn.controller;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/public")
public class PublicController extends BaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/greet")
	public ResponseEntity<?> greet(@CookieValue(name = "_sie9t", required = false) Cookie c) {
		logger.debug("Current thread: " + Thread.currentThread().getId());

		return ResponseEntity.ok("hello");
	}

}
