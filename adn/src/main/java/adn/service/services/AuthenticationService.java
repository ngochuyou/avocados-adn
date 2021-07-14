/**
 * 
 */
package adn.service.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.Cookie;

import org.springframework.security.core.userdetails.UserDetails;

import adn.application.context.ConfigurationContext;
import adn.service.internal.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Service
public class AuthenticationService implements Service {

	// JWT Authenticate Services below
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().setSigningKey(ConfigurationContext.getJwtSecretKey()).parseClaimsJws(token).getBody();
	}

	private Boolean isTokenExpired(String token) {

		return extractExpiration(token).before(new Date());
	}

	public String generateToken(UserDetails userDetails) {
		return createToken(new HashMap<>(), userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
				.signWith(SignatureAlgorithm.HS256, ConfigurationContext.getJwtSecretKey()).compact();
	}

	public Boolean validateToken(String token, String username) {
		return (username.equals(username) && !isTokenExpired(token));
	}

	public Cookie createSessionCookie(String value, String path, boolean secured) {
		Cookie c = new Cookie(ConfigurationContext.getJwtCookieName(), value);

		c.setPath(path);
		c.setSecure(secured);
		c.setHttpOnly(true);
		
		return c;
	}

}
