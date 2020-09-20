/**
 * 
 */
package adn.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;

import adn.application.context.ConfigurationsBuilder;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class SimpleJwtLogoutFilter extends GenericFilterBean {

	protected RequestMatcher logoutRequestMatcher;

	@Autowired
	public SimpleJwtLogoutFilter() {
		this.logoutRequestMatcher = new AntPathRequestMatcher("/auth/logout", HttpMethod.POST.name());
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (this.logoutRequestMatcher.matches(request)) {
			this.logout(request, response);

			return;
		}

		chain.doFilter(request, response);
	}

	protected void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Cookie c = WebUtils.getCookie(request, ConfigurationsBuilder.securityResource.jwtCookieName);

		if (c == null) {
			response.getWriter().print(HttpStatus.NOT_MODIFIED.name());
			response.getWriter().flush();
			response.setStatus(HttpStatus.NOT_MODIFIED.ordinal());

			return;
		}
		// begin-validate token if required
		// end-validate token if required
		c.setHttpOnly(true);
		c.setSecure(false);
		c.setPath("/");
		c.setValue("foot_trace");
		response.addCookie(c);
		response.getWriter().print(HttpStatus.OK.name());
		response.getWriter().flush();
		response.setStatus(HttpStatus.OK.ordinal());
	}

}
