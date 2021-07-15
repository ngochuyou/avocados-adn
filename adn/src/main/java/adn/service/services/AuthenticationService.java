/**
 * 
 */
package adn.service.services;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.Cookie;

import adn.application.context.ConfigurationContext;
import adn.security.ApplicationUserDetails;
import adn.security.ApplicationUserDetailsService;
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

	private static final String VERSION_KEY = "version";
	private static final int EXPIRE_DAYS = 14;

	// JWT Authenticate Services below
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Object extractVersion(String token) {
		return extractClaim(token, claims -> claims.get(VERSION_KEY));
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

	public String generateToken(ApplicationUserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();

		claims.put(VERSION_KEY, userDetails.getVersion());

		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(Date.from(LocalDate.now().plusDays(EXPIRE_DAYS)
						.atStartOfDay(ApplicationUserDetailsService.ZONE).toInstant()))
				.signWith(SignatureAlgorithm.HS256, ConfigurationContext.getJwtSecretKey()).compact();
	}

	public Boolean validateToken(String token, String username, long version) {
		String id = extractUsername(token);
		Object extractedVersion = extractVersion(token);

		if (extractedVersion == null || id == null) {
			return false;
		}

		return (id.equals(username) && Long.valueOf((int) extractedVersion) == version && !isTokenExpired(token));
	}

	public Cookie createSessionCookie(String value, String path, boolean secured) {
		Cookie c = new Cookie(ConfigurationContext.getJwtCookieName(), value);

		c.setPath(path);
		c.setSecure(secured);
		c.setHttpOnly(true);

		return c;
	}

}
