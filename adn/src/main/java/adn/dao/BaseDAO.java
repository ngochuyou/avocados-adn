/**
 * 
 */
package adn.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import adn.application.managers.SpecificationFactory;
import adn.model.Model;
import adn.model.ModelResult;
import adn.model.specification.Specification;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class BaseDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private SpecificationFactory specificationFactory;

	private static final String EXISTED = "Resource already existed";

	private static final String NOT_FOUND = "Resource not found";

	public <T extends Model> T findById(Serializable id, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();

		return session.get(clazz, id);
	}

	public <T extends Model> T findOne(CriteriaQuery<T> query, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		Query<T> hql = session.createQuery(query);

		return hql.getResultStream().findFirst().orElse(null);
	}

	public <T extends Model> List<T> find(CriteriaQuery<T> query, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		Query<T> hql = session.createQuery(query);

		return hql.getResultList();
	}

	public <T extends Model> ModelResult<T> insert(T instance, Class<T> clazz) {
		if (instance == null || clazz == null) {

			return ModelResult.error(Set.of(ModelResult.NULL), instance, null);
		}

		if (this.findById(instance.getId(), clazz) != null) {

			return ModelResult.error(Set.of(ModelResult.CONFLICT), instance, Map.of("id", EXISTED));
		}

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		ModelResult<T> result = specification.isSatisfiedBy(instance);
		Session session = sessionFactory.getCurrentSession();

		if (result.isOk()) {
			session.save(instance);
		} else {
			session.evict(instance);
		}

		return result;
	}

	public <T extends Model> ModelResult<T> update(T instance, Class<T> clazz) {
		if (instance == null || clazz == null) {

			return ModelResult.error(Set.of(ModelResult.NULL), instance, Map.of());
		}

		if (this.findById(instance.getId(), clazz) == null) {

			return ModelResult.error(Set.of(ModelResult.CONFLICT), instance, Map.of("id", NOT_FOUND));
		}

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		Session session = sessionFactory.getCurrentSession();
		ModelResult<T> result = specification.isSatisfiedBy(instance);

		if (result.isOk()) {
			session.update(instance);
		} else {
			session.evict(instance);
		}

		return result;
	}

	public <T extends Model> ModelResult<T> remove(T instance, Class<T> clazz) {
		if (instance == null || clazz == null) {

			return ModelResult.error(Set.of(ModelResult.NULL), instance, Map.of());
		}

		if (this.findById(instance.getId(), clazz) == null) {

			return ModelResult.error(Set.of(ModelResult.CONFLICT), instance, Map.of("id", NOT_FOUND));
		}

		Session session = sessionFactory.getCurrentSession();

		instance.setActive(false);
		session.update(instance);

		return ModelResult.success(instance);
	}

}
