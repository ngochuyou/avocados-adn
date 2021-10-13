/**
 * 
 */
package adn.service.services;

import static adn.application.context.ContextProvider.getPrincipalName;
import static adn.application.context.ContextProvider.getPrincipalRole;
import static adn.application.context.builders.DepartmentScopeContext.unknown;
import static adn.service.internal.Role.HEAD;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.servlet.http.Cookie;

import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.application.context.builders.ConfigurationContext;
import adn.application.context.builders.DepartmentScopeContext;
import adn.controller.exception.UnauthorisedDepartmentException;
import adn.model.entities.Customer;
import adn.model.entities.Head;
import adn.model.entities.Operator;
import adn.model.entities.Personnel;
import adn.security.PersonnelDetails;
import adn.security.UserDetailsImpl;
import adn.security.UserDetailsServiceImpl;
import adn.service.internal.Role;
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

	/**
	 * 
	 */
	private static final String ROLE_MISMATCH_TEMPLATE = "Cannot resolve role [%s] to %s";
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

	public String generateToken(UserDetailsImpl userDetails) {
		Map<String, Object> claims = new HashMap<>();

		claims.put(VERSION_KEY, userDetails.getVersion());

		return createToken(claims, userDetails.getUsername());
	}

	private String createToken(Map<String, Object> claims, String subject) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(Date.from(
						LocalDate.now().plusDays(EXPIRE_DAYS).atStartOfDay(UserDetailsServiceImpl.ZONE).toInstant()))
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

	public UUID getPrincipalDepartment() {
		UserDetailsImpl userDetails = ContextProvider.getPrincipal();

		if (!(userDetails instanceof PersonnelDetails)) {
			return DepartmentScopeContext.unknown();
		}

		return ((PersonnelDetails) userDetails).getDepartmentId();
	}

	public UUID assertSaleDepartment() {
		if (getPrincipalRole() == HEAD) {
			return unknown();
		}

		UUID principalDepartment = getPrincipalDepartment();

		assertDepartment(principalDepartment, DepartmentScopeContext.sale());

		return principalDepartment;
	}

	public UUID assertStockDepartment() {
		if (getPrincipalRole() == HEAD) {
			return unknown();
		}

		UUID principalDepartment = getPrincipalDepartment();

		assertDepartment(principalDepartment, DepartmentScopeContext.stock());

		return principalDepartment;
	}

	public UUID assertPersonnelDepartment() {
		if (getPrincipalRole() == HEAD) {
			return unknown();
		}

		UUID principalDepartment = getPrincipalDepartment();

		assertDepartment(principalDepartment, DepartmentScopeContext.personnel());

		return principalDepartment;
	}

	public UUID assertDepartment(UUID... criterias) throws UnauthorisedDepartmentException {
		if (getPrincipalRole() == HEAD) {
			return unknown();
		}

		UUID principalDepartment = getPrincipalDepartment();

		for (UUID criteria : criterias) {
			if (criteria.equals(principalDepartment)) {
				return principalDepartment;
			}
		}

		throw new UnauthorisedDepartmentException(
				String.format("Department of id [%s] was denied", principalDepartment));
	}

	public boolean isPersonnelDepartment() {
		return getPrincipalDepartment().equals(DepartmentScopeContext.personnel());
	}

	public Operator getOperator() {
		Role principalRole = getPrincipalRole();
		String principalName = getPrincipalName();

		if (principalRole == Role.PERSONNEL) {
			return new Personnel(principalName);
		}

		if (principalRole == HEAD) {
			return new Head(principalName);
		}

		throw new IllegalArgumentException(
				String.format("Invalid %s: [%s]", Operator.class.getSimpleName(), principalName));
	}
	
	public Head getHead() {
		Assert.isTrue(getPrincipalRole() == HEAD, String.format(ROLE_MISMATCH_TEMPLATE, getPrincipalRole(), Head.class.getName()));
		
		return new Head(getPrincipalName());
	}
	
	public Customer getCustomer() {
		Assert.isTrue(getPrincipalRole() == Role.CUSTOMER, String.format(ROLE_MISMATCH_TEMPLATE, getPrincipalRole(), Customer.class.getName()));
		
		return new Customer(getPrincipalName());
	}

}
