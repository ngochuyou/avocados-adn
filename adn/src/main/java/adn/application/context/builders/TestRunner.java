/**
 * 
 */
package adn.application.context.builders;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;

/**
 * @author Ngoc Huy
 *
 */
@Component
@SuppressWarnings("all")
public class TestRunner implements ContextBuilder {

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
