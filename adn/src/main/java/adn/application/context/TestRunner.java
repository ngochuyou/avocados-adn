/**
 * 
 */
package adn.application.context;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.dao.generic.Repository;
import adn.service.internal.CRUDService;
import adn.service.services.GenericCRUDService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@SuppressWarnings("all")
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class TestRunner implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private final int loopT = 10000;

	@Autowired
	private SessionFactoryImplementor sfi;

	@Autowired
	private Repository repo;

	@Autowired
	private CRUDService service;

	@Override
	@Transactional
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
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
