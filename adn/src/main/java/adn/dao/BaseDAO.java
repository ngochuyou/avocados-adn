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

import adn.dao.generic.EntityGeneBuilder;
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

		EntityGeneBuilder<T> geneBuilder = new EntityGeneBuilder<>(clazz);

		instance = geneBuilder.insertion().build(instance);

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		Result<T> result = specification.isSatisfiedBy(instance);
		Session session = sessionFactory.getCurrentSession();

		if (result.isOk()) {
			session.save(instance);

			return result;
		}

		session.evict(instance);

		return result;
	}

	public <T extends Entity, A extends T> Result<A> update(A model, Class<A> newPersistedClass, Class<T> oldType) {
		if (model == null || oldType == null || newPersistedClass == null) {
			return Result.error(HttpStatus.BAD_REQUEST.value(), model, Map.of());
		}

		Session session = sessionFactory.getCurrentSession();

		new EntityGeneBuilder<>(newPersistedClass).update().build(model);
		A persistence = session.load(newPersistedClass, model.getId());

		Specification<A> specification = specificationFactory.getSpecification(newPersistedClass);
		Result<A> result = specification.isSatisfiedBy(persistence);

		if (result.isOk()) {
			session.update(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	/**
	 * This method was deprecated since updating the DTYPE is a very bad design.
	 * <p>
	 * Willingly, this action could be accomplished by deleting the old entity and
	 * saving a new one of the desired type
	 * </p>
	 * 
	 */
	@Deprecated
	public <T extends Entity, A extends T> Result<A> updateDType(A instance, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		Query<?> query = session
				.createNativeQuery("UPDATE " + reflector.getTableName(clazz) + " e SET DTYPE = :type WHERE e.id = :id");

		query.setParameter("type", reflector.getEntityName(instance.getClass()));
		query.setParameter("id", instance.getId());

		int result = query.executeUpdate();

		if (result == 0) {
			return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), instance,
					Map.of("id", "Can not update DTYPE"));
		}

		return Result.success(instance);
	}

}
