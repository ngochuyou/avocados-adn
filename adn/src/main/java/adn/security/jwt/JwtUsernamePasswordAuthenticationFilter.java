/**
 * 
 */
package adn.security.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import adn.security.ApplicationUserDetails;
import adn.security.context.OnMemoryUserContext;
import adn.service.services.AuthenticationService;
import io.jsonwebtoken.lang.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class JwtUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private final AuthenticationService authService;
	private final OnMemoryUserContext onMemUserContext;

	private static final String TOKEN_ENDPOINT = "/auth/token";

	/**
	 * @param requiresAuthenticationRequestMatcher
	 */
	public JwtUsernamePasswordAuthenticationFilter(AuthenticationService authService,
			OnMemoryUserContext onMemUserContext) {
		// TODO Auto-generated constructor stub
		super(new AntPathRequestMatcher(TOKEN_ENDPOINT, HttpMethod.POST.name()));

		Assert.notNull(authService);
		this.authService = authService;
		this.onMemUserContext = onMemUserContext;
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
		ApplicationUserDetails userDetails = (ApplicationUserDetails) authResult.getPrincipal();

		onMemUserContext.put(userDetails);

		String jwt = authService.generateToken(userDetails);
		Cookie cookie = authService.createSessionCookie(jwt, "/", false);

		cookie.setMaxAge(7 * 24 * 60 * 60);// 7 days
		response.addCookie(cookie);
		response.getWriter().print("LOGGED_IN");
		response.getWriter().flush();
		response.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(HttpStatus.OK.value());
	}

}
