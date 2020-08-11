/**
 * 
 */
package adn.application.managers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.application.ApplicationManager;
import adn.model.entities.Admin;
import adn.utilities.Gender;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(1)
public class DatabaseInitializer implements ApplicationManager {

	@Autowired
	private SessionFactory sessionFactory;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		Session session = sessionFactory.getCurrentSession();
		Admin admin = new Admin();

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
		
		logger.info("Finished initializing " + this.getClass().getName());
	}

}
