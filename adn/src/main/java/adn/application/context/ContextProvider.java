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

import adn.security.ApplicationUserDetails;
import adn.utilities.Role;

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

		return ((ApplicationUserDetails) auth.getPrincipal()).getRole();
	}

	public static String getPrincipalName() {

		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

}
