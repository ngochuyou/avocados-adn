/**
 * 
 */
package adn.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import adn.security.JwtUtils;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Controller
@RequestMapping("/auth")
public class AuthenticationController extends BaseController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtils jwtUtils;

	@PostMapping(value = "/token", consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE })
	public @ResponseBody ResponseEntity<?> authenticate(
			@RequestParam(name = "username", required = true) String username,
			@RequestParam(name = "password", required = true) String password, HttpServletRequest request,
			HttpServletResponse response, @CookieValue(name = "JWT", required = false) Cookie c) {
		if (c == null) {
			System.err.println("Cookie not found.");
		} else {
			System.out.println("Found a cookie with value: " + c.getValue());
		}

		if (Strings.isEmpty(username) || Strings.isEmpty(password)) {
			return ResponseEntity.badRequest().body("Credentials required");
		}

		Authentication auth;

		try {
			auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (BadCredentialsException e) {
			return ResponseEntity.badRequest().body("BAD CREDENTIALS");
		}

		UserDetails userDetails = (UserDetails) auth.getPrincipal();

		String jwt = jwtUtils.generateToken(userDetails);
		// create a cookie
		Cookie cookie = new Cookie("JWT", jwt);

		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60); // expires in 7 days
		cookie.setSecure(false);
		cookie.setHttpOnly(true);
		// add cookie to response
		response.addCookie(cookie);
		response.setHeader("Access-Control-Allow-Credentials", "true");

		return ResponseEntity.ok("OK");
	}

}
