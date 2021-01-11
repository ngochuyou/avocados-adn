/**
 * 
 */
package adn.application.context;

import java.util.Arrays;

import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.Status;
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

import adn.service.resource.LocalFileReader;
import adn.service.resource.persistence.ResourceKey;
import adn.service.resource.persistence.ResourcePersistenceContext;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTestRunner implements ContextBuilder {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ResourcePersistenceContext context;
	
	@Autowired
	private LocalFileReader reader;
	
	@Override
	@Transactional
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());
		
		byte[] bytes = reader.read("alesia-kazantceva-XLm6-fPwK5Q-unsplash.jpg");
		ResourceKey key = new ResourceKey("alesia-kazantceva-XLm6-fPwK5Q-unsplash.jpg", byte[].class);
		
		context.addResource(bytes, Status.MANAGED, bytes, key, bytes, LockModeType.NONE, byte[].class);
		
		Object cand = context.getResource(key);
		
		System.out.println(cand);
		
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
