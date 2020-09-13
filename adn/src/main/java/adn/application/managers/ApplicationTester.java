/**
 * 
 */
package adn.application.managers;

import java.util.Arrays;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.application.ContextProvider;
import adn.application.ApplicationManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ApplicationManager {

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
	}

	protected void injectPrincipal() {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("ngochuy.ou", "password"));
	}

	protected void ejectPrincipal() {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("ANONYMOUS_USER",
				"ANONYMOUS_USER", Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
	}

	protected void openSession(FlushMode mode) {
		ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession()
				.setHibernateFlushMode(mode);
	}

	protected void cleanUpSession(boolean flush) {
		Session session = ContextProvider.getApplicationContext().getBean(SessionFactory.class)
				.getCurrentSession();

		if (flush) {
			session.flush();

			return;
		}

		session.clear();
	}

}
