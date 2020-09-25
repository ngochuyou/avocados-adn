/**
 * 
 */
package adn.service.services;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import adn.application.context.ConfigurationsBuilder;
import adn.security.ApplicationUserDetails;
import adn.service.ApplicationService;
import adn.utilities.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class AuthenticationService implements ApplicationService {

	// TODO JWT Authenticate Services below
	public static final String JWT_USERDETAILS_CLAIM_KEY = "userDetails";

	public String extractUsername(String token) {

		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {

		return extractClaim(token, Claims::getExpiration);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public UserDetails extractUserDetails(String token) {
		Map<String, Object> o = (Map<String, Object>) extractClaim(token,
				claims -> claims.get(JWT_USERDETAILS_CLAIM_KEY));
		UserDetails userDetails = null;

		try {
			userDetails = new ApplicationUserDetails(o.get("username").toString(), "", (boolean) o.get("enabled"),
					(boolean) o.get("accountNonExpired"), (boolean) o.get("credentialsNonExpired"),
					(boolean) o.get("accountNonLocked"),
					(Collection<? extends GrantedAuthority>) ((List<?>) o.get("authorities")).stream()
							.map(v -> new SimpleGrantedAuthority(((Map) v).get("authority").toString()))
							.collect(Collectors.toSet()),
					Role.valueOf(o.get("role").toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return userDetails;
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

		return claimsResolver.apply(extractAllClaims(token));
	}

	private Claims extractAllClaims(String token) {

		return Jwts.parser().setSigningKey(ConfigurationsBuilder.securityResource.jwtSecretKey).parseClaimsJws(token)
				.getBody();
	}

	private Boolean isTokenExpired(String token) {

		return extractExpiration(token).before(new Date());
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();

		claims.put(JWT_USERDETAILS_CLAIM_KEY, userDetails);

		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
				.signWith(SignatureAlgorithm.HS256, ConfigurationsBuilder.securityResource.jwtSecretKey).compact();
	}

	public Boolean validateToken(String token, String username) {

		return (username.equals(username) && !isTokenExpired(token));
	}

	public Cookie createSessionCookie(String name, String value, String path, boolean secured) {
		Cookie c = new Cookie(name, value);

		c.setPath(path);
		c.setSecure(secured);
		c.setHttpOnly(true);

		return c;
	}

}
