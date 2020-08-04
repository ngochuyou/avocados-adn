/**
 * 
 */
package adn.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import adn.application.managers.ConfigurationsManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private ApplicationUserDetailsService userDetailService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		final String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith(ConfigurationsManager.securityResource.jwtAuthHeaderValue)) {
			logger.debug("JWT filtering request");
			System.out
					.println(WebUtils.getCookie(request, ConfigurationsManager.securityResource.jwtCookieName) == null);
			String jwt = WebUtils.getCookie(request, ConfigurationsManager.securityResource.jwtCookieName).getValue();
			String username = jwtUtils.extractUsername(jwt);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UserDetails userDetails = userDetailService.loadUserByUsername(username);

				if (jwtUtils.validateToken(jwt, userDetails)) {
					UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());

					token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(token);
				}
			}
		}

		filterChain.doFilter(request, response);
	}

}
