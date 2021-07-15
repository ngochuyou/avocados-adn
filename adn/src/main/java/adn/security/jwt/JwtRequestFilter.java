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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import adn.application.context.ConfigurationContext;
import adn.security.ApplicationUserDetails;
import adn.security.ApplicationUserDetailsService;
import adn.security.context.OnMemoryUserContext;
import adn.service.services.AuthenticationService;
import io.jsonwebtoken.ExpiredJwtException;

/**
 * @author Ngoc Huy
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

	@Autowired
	private AuthenticationService authService;

	@Autowired
	private ApplicationUserDetailsService userDetailsService;

	@Autowired
	private OnMemoryUserContext onMemUserContext;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith(ConfigurationContext.getJwtAuthHeaderValue())) {
			Cookie c = WebUtils.getCookie(request, ConfigurationContext.getJwtCookieName());

			if (c != null) {
				String jwt = c.getValue();
				String username = null;

				try {
					username = authService.extractUsername(jwt);
				} catch (ExpiredJwtException e) {
					filterChain.doFilter(request, response);
					return;
				}

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					ApplicationUserDetails userDetails;

					if ((userDetails = onMemUserContext.getUser(username)) == null) {
						userDetails = (ApplicationUserDetails) userDetailsService.loadUserByUsername(username);
					}

					if (authService.validateToken(jwt, userDetails.getUsername(), userDetails.getVersion())) {
						UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
								null, userDetails.getAuthorities());

						token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						SecurityContextHolder.getContext().setAuthentication(token);
					}
				}

				filterChain.doFilter(request, response);
				return;
			}

			if (logger.isTraceEnabled()) {
				logger.trace("Unable to locate JWT cookie");
				filterChain.doFilter(request, response);
				return;
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("JWT header not found");
		}

		filterChain.doFilter(request, response);
	}

}
