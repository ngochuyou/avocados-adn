/**
 * 
 */
package adn.application.context;

import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.helpers.Role;
import adn.security.ApplicationUserDetails;
import adn.service.resource.local.ManagerFactoryEventListener;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ContextProvider implements ApplicationContextAware, ManagerFactoryEventListener {

	private static ApplicationContext applicationContext;
	private static EntityManagerFactoryImplementor LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE;

	private static Access access = new Access();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		ContextProvider.applicationContext = applicationContext;
		listen();
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

	public static EntityManagerFactoryImplementor getLocalResourceSessionFactory() {
		return LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE;
	}

	public static Access getAccess() throws IllegalAccessException {
		if (access == null) {
			throw new IllegalAccessException("Access was closed");
		}

		return access;
	}

	public static class Access {

		public void setLocalResourceSessionFactory(
				EntityManagerFactoryImplementor LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE) {
			LoggerFactory.getLogger(
					"Setting an instance of " + EntityManagerFactoryImplementor.class + " to " + this.getClass());
			ContextProvider.LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE = LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE;
		}

	}

	@Override
	public void postBuild(EntityManagerFactoryImplementor managerFactory) {
		LoggerFactory.getLogger(this.getClass()).trace("Closing access in " + this.getClass());
		access = null;
	}

}
