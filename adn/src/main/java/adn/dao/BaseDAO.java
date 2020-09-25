/**
 * 
 */
package adn.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import adn.model.Result;
import adn.model.entities.Entity;
import adn.model.specification.Specification;
import adn.model.specification.SpecificationFactory;
import adn.utilities.ClassReflector;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Primary
public class BaseDAO {

	@Autowired
	protected ClassReflector reflector;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected SpecificationFactory specificationFactory;

	private static final String EXISTED = "Resource already existed";

	private static final String NOT_FOUND = "Resource not found";

	public <T extends Entity> T findById(Serializable id, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();

		return session.get(clazz, id);
	}

	public <T extends Entity> T findOne(CriteriaQuery<T> query, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		Query<T> hql = session.createQuery(query);

		return hql.getResultStream().findFirst().orElse(null);
	}

	public <T extends Entity> List<T> find(CriteriaQuery<T> query, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		Query<T> hql = session.createQuery(query);

		return hql.getResultList();
	}

	public <T extends Entity> Result<T> insert(T instance, Class<T> clazz) {
		if (instance == null || clazz == null) {
			return Result.error(HttpStatus.BAD_REQUEST.value(), instance, null);
		}

		if (this.findById(instance.getId(), clazz) != null) {
			return Result.error(HttpStatus.CONFLICT.value(), instance, Map.of("id", EXISTED));
		}

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		Result<T> result = specification.isSatisfiedBy(instance);
		Session session = sessionFactory.getCurrentSession();

		if (result.isOk()) {
			session.save(instance);
		} else {
			session.evict(instance);
		}

		return result;
	}

	public <T extends Entity> Result<T> update(T instance, Class<T> clazz) {
		if (instance == null || clazz == null) {
			return Result.error(HttpStatus.BAD_REQUEST.value(), instance, Map.of());
		}

		T persisted;

		if ((persisted = findById(instance.getId(), clazz)) == null) {
			return Result.error(HttpStatus.CONFLICT.value(), instance, Map.of("id", NOT_FOUND));
		}

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		Session session = sessionFactory.getCurrentSession();
		Result<T> result = specification.isSatisfiedBy(instance);

		if (result.isOk()) {
			session.evict(persisted);
			session.update(instance);
		} else {
			session.evict(instance);
		}

		return result;
	}

	public <T extends Entity> Result<T> updateDType(T instance, Class<? extends T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		Query<?> query = session.createQuery("UPDATE Account a SET DTYPE = :type WHERE a.id = :id");

		query.setParameter("type", reflector.getEntityName(clazz));
		query.setParameter("id", instance.getId());

		int result = query.executeUpdate();

		if (result == 0) {
			return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), instance,
					Map.of("id", "Can not update DTYPE"));
		}

		return Result.success(instance);
	}

}
