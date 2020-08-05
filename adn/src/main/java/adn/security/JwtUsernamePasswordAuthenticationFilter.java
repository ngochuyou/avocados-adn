/**
 * 
 */
package adn.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import adn.application.managers.ConfigurationsManager;
import adn.service.AuthenticationService;
import io.jsonwebtoken.lang.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class JwtUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	@Autowired
	private AuthenticationService authService;

	/**
	 * @param requiresAuthenticationRequestMatcher
	 */
	public JwtUsernamePasswordAuthenticationFilter(AuthenticationService authService) {
		super(new AntPathRequestMatcher("/auth/token", HttpMethod.POST.name()));
		Assert.notNull(authService);
		this.authService = authService;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {
		// TODO Auto-generated method stub
		String username = request.getParameter("username");
		String password = request.getParameter("password");

		username = username != null ? username : "";
		password = password != null ? password : "";

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

		return this.getAuthenticationManager().authenticate(token);
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		// TODO Auto-generated method stub
		UserDetails userDetails = (UserDetails) authResult.getPrincipal();
		String jwt = authService.generateToken(userDetails);
		Cookie cookie = authService.createSessionCookie(ConfigurationsManager.securityResource.jwtCookieName, jwt, "/",
				false);

		cookie.setMaxAge(7 * 24 * 60 * 60);// days
		response.addCookie(cookie);
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.getWriter().print("LOGGED_IN");
		response.getWriter().flush();
		response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.OK.ordinal());
	}

	public AuthenticationService getAuthService() {
		return authService;
	}

	public void setAuthService(AuthenticationService authService) {
		this.authService = authService;
	}

}
