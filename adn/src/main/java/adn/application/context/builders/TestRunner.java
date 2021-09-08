/**
 * 
 */
package adn.application.context.builders;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
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

import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.dao.generic.GenericRepository;
import adn.service.internal.GenericCRUDService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@SuppressWarnings("all")
public class TestRunner implements ContextBuilder {

	private final int loopT = 10000;

	@Autowired
	private SessionFactoryImplementor sfi;

	@Autowired
	private GenericRepository repo;

	@Autowired
	private GenericCRUDService service;

	@Override
	@Transactional
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Initializing " + this.getClass().getName());
		logger.info("Finished initializing " + this.getClass().getName());
	}

	@Override
	public void afterBuild() {
		Stream.of(this.getClass().getDeclaredFields()).forEach(f -> {
			try {
				f.set(this, null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
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
