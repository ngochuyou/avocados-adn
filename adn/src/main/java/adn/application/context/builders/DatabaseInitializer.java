/**
 * 
 */
package adn.application.context.builders;

import static adn.application.context.builders.DepartmentScopeContext.CUSTOMERSERVICE_NAME;
import static adn.application.context.builders.DepartmentScopeContext.PERSONNEL_NAME;
import static adn.application.context.builders.DepartmentScopeContext.SALE_NAME;
import static adn.application.context.builders.DepartmentScopeContext.STOCK_NAME;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import adn.application.context.internal.ContextBuilder;
import adn.dao.generic.GenericRepository;
import adn.model.entities.Department;
import adn.model.entities.Head;
import adn.model.entities.constants.Gender;
import adn.service.services.AccountService;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class DatabaseInitializer implements ContextBuilder {

	@Autowired
	private SessionFactory sessionFactory;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private GenericRepository genericRepository;

	@Transactional
	@Override
	public void buildAfterStartUp() {
		// TODO Auto-generated method stub
		logger.info("Building " + this.getClass().getName());
		insertHead();
		insertDepartments();
		logger.info("Finished initializing " + this.getClass().getName());
	}

	@SuppressWarnings("serial")
	private Specification<Department> departmentHasName(String name) {
		return new Specification<Department>() {

			@Override
			public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				return builder.equal(root.get("name"), name);
			}
		};
	}

	private void insertDepartments() {
		Session session = sessionFactory.getCurrentSession();
		Department department;

		if (genericRepository.findOne(Department.class, departmentHasName(STOCK_NAME)).isEmpty()) {
			logger.info("Inserting " + STOCK_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(STOCK_NAME);
			session.save(department);
		}

		if (genericRepository.findOne(Department.class, departmentHasName(SALE_NAME)).isEmpty()) {
			logger.info("Inserting " + SALE_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(SALE_NAME);
			session.save(department);
		}

		if (genericRepository.findOne(Department.class, departmentHasName(PERSONNEL_NAME)).isEmpty()) {
			logger.info("Inserting " + PERSONNEL_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(PERSONNEL_NAME);
			session.save(department);
		}

		if (genericRepository.findOne(Department.class, departmentHasName(CUSTOMERSERVICE_NAME)).isEmpty()) {
			logger.info("Inserting " + CUSTOMERSERVICE_NAME);

			department = new Department();
			department.setActive(Boolean.TRUE);
			department.setName(CUSTOMERSERVICE_NAME);
			session.save(department);
		}
	}

	public Head getAdmin() {
		Head admin = new Head("ngochuy.ou");

		admin.setPassword(passwordEncoder.encode("password"));
		admin.setActive(true);
		admin.setEmail("ngochuy.ou@gmail.com");
		admin.setFirstName("Tran");
		admin.setGender(Gender.MALE);
		admin.setLastName("Vu Ngoc Huy");
		admin.setPhone("0974032706");
		admin.setPhoto(AccountService.DEFAULT_ACCOUNT_PHOTO_NAME);

		return admin;
	}

	private void insertHead() {
		Session session = sessionFactory.getCurrentSession();

		if (session.get(Head.class, "ngochuy.ou") == null) {
			Head head = getAdmin();

			session.save(head);

			logger.info("Inserting HEAD: " + head.getId());
		}
	}
}
