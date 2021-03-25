/**
 * 
 */
package adn.controller;

import java.util.UUID;

import javax.servlet.http.Cookie;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import adn.service.resource.local.ResourceContextImpl;

/**
 * @author Ngoc Huy
 *
 */
@RestController
@RequestMapping("/public")
public class PublicController extends BaseController {

	@Autowired
	private ResourceContextImpl context;

	@GetMapping("/greet")
	public ResponseEntity<?> greet(@CookieValue(name = "_sie9t", required = false) Cookie c) {
		String uuid = UUID.randomUUID().toString();
		context.add(uuid, "val_" + uuid);

		return ResponseEntity.ok("hello");
	}

}
