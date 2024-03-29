/**
 * 
 */
package adn.application.context;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.SessionFactoryImpl;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.model.factory.authentication.Credential;
import adn.security.UserDetailsImpl;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ContextProvider implements ApplicationContextAware {

	private static ApplicationContext applicationContext;
	private static SessionFactoryImpl LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE;

	private static Access access = new Access();

	private ContextProvider() {}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ContextProvider.applicationContext = applicationContext;
	}
	
	public static Session getCurrentSession() {
		return getBean(SessionFactory.class).getCurrentSession();
	}

	public static <T> T getBean(Class<T> beanType) {
		return getApplicationContext().getBean(beanType);
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public static UserDetailsImpl getPrincipal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			return null;
		}

		return (UserDetailsImpl) auth.getPrincipal();
	}

	public static Role getPrincipalRole() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			return Role.ANONYMOUS;
		}
		// handle with care while working with unit testing
		// e.g: using @WithMockUser may make the test unit inject
		// an instance of type org.springframework.security.core.userdetails.User
		// rather than ApplicationUserDetails, which causes the following type-casting
		// to fail
		return ((UserDetailsImpl) auth.getPrincipal()).getRole();
	}

	public static Credential getPrincipalCredential() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof AnonymousAuthenticationToken) {
			return Role.ANONYMOUS;
		}

		return ((UserDetailsImpl) auth.getPrincipal()).getCredential();
	}

	public static String getPrincipalName() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		return auth == null ? null : auth.getName();
	}

	public static SessionFactoryImpl getLocalResourceSessionFactory() {
		return LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE;
	}

	public static Access getAccess() throws IllegalAccessException {
		if (access == null) {
			throw new IllegalAccessException("Access was closed");
		}

		return access;
	}

	/**
	 * Efficient final modifier
	 * 
	 * @author Ngoc Huy
	 *
	 */
	@SuppressWarnings("serial")
	public static class Access implements SessionFactoryObserver {

		public void setLocalResourceSessionFactory(SessionFactoryImpl LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE) {
			LoggerFactory.getLogger(this.getClass())
					.info("Created an instance of " + LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE.getClass());
			ContextProvider.LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE = LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE;
			// register destruction of this LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE to
			// SessionFactoryImplementor's shutdown hook
			applicationContext.getBean(SessionFactory.class).unwrap(SessionFactoryImplementor.class).addObserver(this);
			closeAccess();
		}

		@Override
		public void sessionFactoryClosed(SessionFactory factory) {
			ContextProvider.LOCAL_RESOURCE_SESSION_FACTORY_INSTANCE.close();
		}

	}

	private static void closeAccess() {
		LoggerFactory.getLogger(ContextProvider.class)
				.trace(String.format("Closing access to [%s]", ContextProvider.class));
		access = null;
	}

}
