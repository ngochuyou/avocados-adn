/**
 * 
 */
package adn.application.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.helpers.Role;
import adn.security.ApplicationUserDetails;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ContextProvider implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		ContextProvider.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static Role getPrincipalRole() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			return Role.ANONYMOUS;
		}

		// handle with care when working with unit testing
		// e.g: using @WithMockUser may make the test unit inject
		// an instance of type org.springframework.security.core.userdetails.User
		// rather than ApplicationUserDetails, which causes the following type-casting
		// to fail
		return ((ApplicationUserDetails) auth.getPrincipal()).getRole();
	}

	public static String getPrincipalName() {

		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
