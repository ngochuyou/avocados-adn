/**
 * 
 */
package adn.application.managers;

import java.util.Arrays;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import adn.application.ApplicationManager;

/**
 * @author Ngoc Huy
 *
 */
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ApplicationManager {

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
	}

	public void injectPrincipal() {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("n", "password"));
	}

	public void ejectPrincipal() {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("ANONYMOUS_USER",
				"ANONYMOUS_USER", Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
	}

}
