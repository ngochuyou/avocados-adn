/**
 * 
 */
package adn.application.context;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.application.Constants;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.utilities.Gender;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(1)
public class DatabaseBuilder implements ContextBuilder {

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
		this.insertAdmin();
		this.insertCustomer();
		this.insertManager();
		this.insertEmployee();
		logger.info("Finished initializing " + this.getClass().getName());
	}

	private void insertAdmin() {
		Session session = sessionFactory.getCurrentSession();

		if (session.get(Admin.class, "ngochuy.ou") == null) {
			Admin admin = new Admin();

			admin.setId("ngochuy.ou");
			admin.setPassword(passwordEncoder.encode("password"));
			admin.setActive(true);
			admin.setEmail("ngochuy.ou@gmail.com");
			admin.setFirstName("Tran");
			admin.setGender(Gender.MALE);
			admin.setLastName("Vu Ngoc Huy");
			admin.setPhone("0974032706");
			admin.setPhoto(Constants.DEFAULT_USER_PHOTO_NAME);
			admin.setRole(Role.ADMIN);

			session.save(admin);

			logger.info("Inserting ADMIN: " + admin.getId());
		}
	}

	private void insertCustomer() {
		Session session = sessionFactory.getCurrentSession();

		if (session.get(Customer.class, "adn.customer.0") == null) {
			Customer customer = new Customer();

			customer.setId("adn.customer.0");
			customer.setPassword(passwordEncoder.encode("password"));
			customer.setActive(true);
			customer.setEmail("adn.customer.0@gmail.com");
			customer.setFirstName("Tran");
			customer.setGender(Gender.FEMALE);
			customer.setLastName("Vu Ngoc Huy");
			customer.setPhone("0974032706");
			customer.setPhoto(Constants.DEFAULT_USER_PHOTO_NAME);
			customer.setRole(Role.CUSTOMER);

			session.save(customer);

			logger.info("Inserting CUSTOMER: " + customer.getId());
		}
	}

	private void insertManager() {
		Session session = sessionFactory.getCurrentSession();

		if (session.get(Personnel.class, "adn.personnel.manager.0") == null) {
			Personnel manager = new Personnel();

			manager.setId("adn.personnel.manager.0");
			manager.setPassword(passwordEncoder.encode("password"));
			manager.setActive(true);
			manager.setEmail("adn.personnel.manager.0@gmail.com");
			manager.setFirstName("Tran");
			manager.setGender(Gender.UNKNOWN);
			manager.setLastName("Vu Ngoc Huy");
			manager.setPhone("0974032706");
			manager.setPhoto(Constants.DEFAULT_USER_PHOTO_NAME);
			manager.setRole(Role.PERSONNEL);

			session.save(manager);

			logger.info("Inserting PERSONNEL: " + manager.getId());
		}
	}

	private void insertEmployee() {
		Session session = sessionFactory.getCurrentSession();

		if (session.get(Personnel.class, "adn.personnel.employee.0") == null) {
			Personnel manager = new Personnel();

			manager.setId("adn.personnel.employee.0");
			manager.setPassword(passwordEncoder.encode("password"));
			manager.setActive(true);
			manager.setEmail("adn.personnel.employee.0@gmail.com");
			manager.setFirstName("Tran");
			manager.setGender(Gender.UNKNOWN);
			manager.setLastName("Vu Ngoc Huy");
			manager.setPhone("0974032706");
			manager.setPhoto(Constants.DEFAULT_USER_PHOTO_NAME);
			manager.setRole(Role.PERSONNEL);

			session.save(manager);

			logger.info("Inserting PERSONNEL: " + manager.getId());
		}
	}

}
