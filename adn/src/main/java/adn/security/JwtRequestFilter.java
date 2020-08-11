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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import adn.application.managers.ConfigurationsManager;
import adn.service.AuthenticationService;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	@Autowired
	private AuthenticationService authService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith(ConfigurationsManager.securityResource.jwtAuthHeaderValue)) {
			Cookie c = WebUtils.getCookie(request, ConfigurationsManager.securityResource.jwtCookieName);

			if (c != null) {
				String jwt = c.getValue();
				String username = authService.extractUsername(jwt);

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UserDetails userDetails = authService.extractUserDetails(jwt);

					if (authService.validateToken(jwt, userDetails.getUsername())) {
						UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
								null, userDetails.getAuthorities());

						token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						SecurityContextHolder.getContext().setAuthentication(token);
					}
				}
			}
		}
		
		filterChain.doFilter(request, response);
	}

}