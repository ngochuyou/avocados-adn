/**
 * 
 */
package adn.application.managers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import adn.application.ApplicationManager;
import adn.model.entities.Admin;
import adn.utilities.Gender;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Order(1)
public class DatabaseInitializer implements ApplicationManager {

	private SessionFactory sessionFactory = context.getBean(SessionFactory.class);

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		Session session = sessionFactory.openSession();
		Admin admin = new Admin();

		session.beginTransaction();
		admin.setId("ngochuy.ou");
		admin.setPassword(passwordEncoder.encode("password"));
		admin.setActive(true);
		admin.setEmail("ngochuy.ou@gmail.com");
		admin.setFirstName("Tran");
		admin.setGender(Gender.MALE);
		admin.setLastName("Vu Ngoc Huy");
		admin.setPhone("0974032706");
		admin.setPhoto("default.jpg");
		admin.setRole(Role.ADMIN);

		if (session.get(Admin.class, admin.getId()) == null) {
			session.save(admin);
			logger.info("Inserting ADMIN: " + admin.getId());
		}

		session.flush();
		session.close();
		logger.info("Finished initializing " + this.getClass().getName());
	}

}
