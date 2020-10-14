/**
 * 
 */
package adn.application.context;

import java.io.File;
import java.util.Arrays;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.service.context.ServiceManager;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ServiceManager serviceManager;

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		File file = new File(Constants.IMAGE_FILE_PATH + "aad81c87bd8316705c4568e72577eb62476a.jpg");

		serviceManager.persist(file);

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
