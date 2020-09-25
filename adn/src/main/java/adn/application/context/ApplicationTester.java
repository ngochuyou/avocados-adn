/**
 * 
 */
package adn.application.context;

import java.util.Arrays;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());
		logger.info("Finished initializing " + this.getClass().getName());
	}

	protected void injectPrincipal() {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken("ngochuy.ou", "password"));
	}

	protected void ejectPrincipal() {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken("ANONYMOUS_USER",
				"ANONYMOUS_USER", Arrays.asList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
	}

	protected void cleanUpSession(boolean flush) {
		Session session = ContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession();

		if (flush) {
			session.flush();

			return;
		}

		session.clear();
	}

}
