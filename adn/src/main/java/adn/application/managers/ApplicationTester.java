/**
 * 
 */
package adn.application.managers;

import java.util.Arrays;
import java.util.Date;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import adn.application.ApplicationContextProvider;
import adn.application.ApplicationManager;
import adn.model.entities.Admin;
import adn.model.models.AdminModel;
import adn.utilities.Gender;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(value = Ordered.LOWEST_PRECEDENCE)
public class ApplicationTester implements ApplicationManager {

	@Autowired
	private AuthenticationBasedEMFactory factory;

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		Admin admin = new Admin();

		admin.setId("ngohuyou");
		admin.setRole(Role.ADMIN);
		admin.setGender(Gender.MALE);
		admin.setContractDate(new Date());

		long t = System.currentTimeMillis();
		AdminModel model = factory.produce(admin, AdminModel.class, null);

		System.out.println(System.currentTimeMillis() - t);
		System.out.println(model.getContractDate());
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
		ApplicationContextProvider.getApplicationContext().getBean(SessionFactory.class).getCurrentSession()
				.setHibernateFlushMode(mode);
	}

	protected void cleanUpSession(boolean flush) {
		Session session = ApplicationContextProvider.getApplicationContext().getBean(SessionFactory.class)
				.getCurrentSession();

		if (flush) {
			session.flush();

			return;
		}

		session.clear();
	}

}
