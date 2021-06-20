/**
 * 
 */
package adn.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import adn.dao.generic.EntityGeneBuilder;
import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;
import adn.model.specification.Specification;
import adn.model.specification.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
@Repository
@Primary
public class BaseDAO {

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	protected SpecificationFactory specificationFactory;

	public <T extends Entity> T findById(Serializable id, Class<T> clazz) {
		return sessionFactory.getCurrentSession().find(clazz, id);
	}

	public <T extends Entity> T findOne(CriteriaQuery<T> query, Class<T> clazz) {
		// @formatter:off
		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultStream().findFirst().orElse(null);
		// @formatter:on
	}

	public <T extends Entity> List<T> find(CriteriaQuery<T> query, Class<T> clazz) {
		// @formatter:off
		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
		// @formatter:on
	}

	public <T extends Entity> DatabaseInteractionResult<T> insert(T instance, Class<T> clazz) {
		EntityGeneBuilder<T> geneBuilder = new EntityGeneBuilder<>(clazz);

		instance = geneBuilder.insertion().build(instance);

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		DatabaseInteractionResult<T> result = specification.isSatisfiedBy(instance);
		Session session = sessionFactory.getCurrentSession();

		if (result.isOk()) {
			session.save(instance);

			return result;
		}

		session.evict(instance);

		return result;
	}

	public <T extends Entity> DatabaseInteractionResult<T> update(T model, Class<T> type) {
		Session session = sessionFactory.getCurrentSession();

		new EntityGeneBuilder<>(type).update().build(model);
		T persistence = session.load(type, model.getId());

		Specification<T> specification = specificationFactory.getSpecification(type);
		DatabaseInteractionResult<T> result = specification.isSatisfiedBy(persistence);

		if (result.isOk()) {
			session.update(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

}
