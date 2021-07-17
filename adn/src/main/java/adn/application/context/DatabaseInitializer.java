/**
 * 
 */
package adn.application.context;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.dao.Repository;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Department;
import adn.model.entities.DepartmentChief;
import adn.model.entities.Personnel;
import adn.model.entities.Provider;
import adn.model.entities.constants.Gender;
import adn.model.entities.id.DepartmentChiefId;
import adn.service.internal.Role;
import adn.service.services.AccountService;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(1)
public class DatabaseInitializer implements ContextBuilder {

	@Autowired
	private SessionFactory sessionFactory;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private Repository repo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private Environment env;

	@Transactional
	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass().getName());

		insertAdmin();

		if (!env.getProperty("spring.profiles.active").equals("PROD")) {
			sessionFactory.getCurrentSession().setHibernateFlushMode(FlushMode.MANUAL);

			insertMockDepartment();
			insertMockProviders();
			insertCustomer();
			insertMockPersonnel();
			insertMockDepartmentChief();

			sessionFactory.getCurrentSession().flush();
		}

		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("unused")
	private void assignDepartment() {
		Session ss = sessionFactory.getCurrentSession();

		List<Department> departments = repo.fetch(Department.class);
		int max = departments.size() - 1;

		List<Personnel> personnels = repo.fetch(Personnel.class, PageRequest.of(0, 1000));

		for (Personnel p : personnels) {
			if (p.getDepartment() == null) {
				p.setDepartment(departments.get(rand(max, 0)));
			}

			ss.save(p);
		}
	}

	private void insertMockDepartmentChief() {
		if (!repo.fetch(DepartmentChief.class).isEmpty()) {
			return;
		}

		Session ss = sessionFactory.getCurrentSession();
		List<Department> departments = repo.fetch(Department.class);
		List<Personnel> personnels = repo.fetch(Personnel.class, PageRequest.of(0, departments.size()));
		DepartmentChief chief;
		DepartmentChiefId id;
		Department dep;
		Personnel per;

		for (int i = 0; i < personnels.size() && i < departments.size(); i++) {
			dep = departments.get(i);
			per = personnels.get(i);

			chief = new DepartmentChief();
			chief.setDepartment(dep);
			chief.setPersonnel(per);

			id = new DepartmentChiefId();
			id.setStartDate(LocalDate.now());

			chief.setId(id);
			ss.save(chief);
		}
	}

	private void insertMockDepartment() {
		if (!repo.fetch(Department.class).isEmpty()) {
			return;
		}

		Department dep;
		Session session = sessionFactory.getCurrentSession();

		dep = new Department();

		dep.setName("Stock");
		dep.setActive(true);

		session.save(dep);

		dep = new Department();

		dep.setName("Sale");
		dep.setActive(true);

		session.save(dep);

		dep = new Department();

		dep.setName("Personnel");
		dep.setActive(true);

		session.save(dep);

		dep = new Department();

		dep.setName("Finance");
		dep.setActive(true);

		session.save(dep);
	}

	private void insertMockPersonnel() {
		if (repo.fetch(Personnel.class).size() != 0) {
			return;
		}

		List<Department> departments = repo.fetch(Department.class);

		if (departments.size() == 0) {
			return;
		}

		int max = departments.size() - 1;

		Session session = sessionFactory.getCurrentSession();
		Personnel personnel = new Personnel();

		personnel.setId("lawrence.p.penney");
		personnel.setPassword(passwordEncoder.encode("password"));
		personnel.setActive(true);
		personnel.setEmail("justina_wilkins@gmail.com");
		personnel.setFirstName("Lawrence");
		personnel.setLastName("P Penney");
		personnel.setGender(Gender.MALE);
		personnel.setPhone("978-224-3032");
		personnel.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
		personnel.setRole(Role.PERSONNEL);
		personnel.setCreatedBy("ngochuy.ou");
		personnel.setDepartment(departments.get(rand(max, 0)));

		session.save(personnel);

		personnel = new Personnel();

		personnel.setId("timothy_smith");
		personnel.setPassword(passwordEncoder.encode("password"));
		personnel.setActive(true);
		personnel.setEmail("t.smith04@adn.com");
		personnel.setFirstName("Timothy");
		personnel.setLastName("Smith");
		personnel.setGender(Gender.MALE);
		personnel.setPhone("+1 270 419-3852");
		personnel.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
		personnel.setRole(Role.PERSONNEL);
		personnel.setCreatedBy("ngochuy.ou");
		personnel.setDepartment(departments.get(rand(max, 0)));

		session.save(personnel);

		personnel = new Personnel();

		personnel.setId("angelo_jeffson");
		personnel.setPassword(passwordEncoder.encode("password"));
		personnel.setActive(true);
		personnel.setEmail("krystina1978@hotmail.com");
		personnel.setFirstName("Angelo");
		personnel.setLastName("B Jefferson");
		personnel.setGender(Gender.MALE);
		personnel.setPhone("469-467-9379");
		personnel.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
		personnel.setRole(Role.PERSONNEL);
		personnel.setCreatedBy("ngochuy.ou");
		personnel.setDepartment(departments.get(rand(max, 0)));

		session.save(personnel);

		personnel = new Personnel();

		personnel.setId("biglion949");
		personnel.setPassword(passwordEncoder.encode("password"));
		personnel.setActive(true);
		personnel.setEmail("seyhan.prakken@ex.com");
		personnel.setFirstName("Seyhan");
		personnel.setLastName("Parkken");
		personnel.setGender(Gender.MALE);
		personnel.setPhone("(843)-440-2148");
		personnel.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
		personnel.setRole(Role.PERSONNEL);
		personnel.setCreatedBy("ngochuy.ou");
		personnel.setDepartment(departments.get(rand(max, 0)));

		session.save(personnel);
	}

	private void insertMockProviders() {
		if (!repo.fetch(Provider.class).isEmpty()) {
			return;
		}

		Provider provider;
		Session session = sessionFactory.getCurrentSession();

		provider = new Provider();

		provider.setName("MUK Ltd. Vietnam");
		provider.setCreatedBy("ngochuy.ou");
		provider.setEmail("muk.ltd@hotmail.com");
		provider.setAddress("34 St.Saint-Étienne, Sao Paulo, Brazil");
		provider.setPhoneNumbers(Set.of("+554139087774", "+5541149505877"));
		provider.setRepresentatorName("Maul U. Kerian");
		provider.setUpdatedBy("ngochuy.ou");
		provider.setActive(Boolean.TRUE);

		session.save(provider);

		provider = new Provider();

		provider.setName("Thai Son S.P Sewing Factory");
		provider.setCreatedBy("ngochuy.ou");
		provider.setEmail("contact-support@thaison.com");
		provider.setAddress("SA8000 BSCI");
		provider.setPhoneNumbers(Set.of("+84976538642", "+84976538643"));
		provider.setRepresentatorName("Huynh Ngoc Tram");
		provider.setUpdatedBy("ngochuy.ou");
		provider.setActive(Boolean.TRUE);

		session.save(provider);

		provider = new Provider();

		provider.setName("Dong Nai Industrial Garment Company");
		provider.setCreatedBy("ngochuy.ou");
		provider.setEmail("dongnaigarment@dnco.vn.com");
		provider.setAddress("Bien Hoa City");
		provider.setPhoneNumbers(Set.of("+8418232764", "+84909765182"));
		provider.setRepresentatorName("Textile Hanibal");
		provider.setUpdatedBy("ngochuy.ou");
		provider.setActive(Boolean.TRUE);

		session.save(provider);
	}

	public Admin getAdmin() {
		Admin admin = new Admin();

		admin.setId("ngochuy.ou");
		admin.setPassword(passwordEncoder.encode("password"));
		admin.setActive(true);
		admin.setEmail("ngochuy.ou@gmail.com");
		admin.setFirstName("Tran");
		admin.setGender(Gender.MALE);
		admin.setLastName("Vu Ngoc Huy");
		admin.setPhone("0974032706");
		admin.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
		admin.setRole(Role.ADMIN);

		return admin;
	}

	private void insertAdmin() {
		Session session = sessionFactory.getCurrentSession();

		if (session.get(Admin.class, "ngochuy.ou") == null) {
			Admin admin = getAdmin();

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
			customer.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);
			customer.setRole(Role.CUSTOMER);

			session.save(customer);

			logger.info("Inserting CUSTOMER: " + customer.getId());
		}
	}

	private final Random random = new Random();

	private int rand(int max, int min) {
		return random.nextInt(max - min + 1) + min;
	}

}
