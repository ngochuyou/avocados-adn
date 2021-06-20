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
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;
import adn.model.specification.Specification;
import adn.model.specification.SpecificationFactory;
import adn.service.entity.CompositeGeneBuilder;
import adn.service.entity.GeneBuilder;

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
		return sessionFactory.getCurrentSession().get(clazz, id);
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

	@SuppressWarnings("unchecked")
	public <T extends Entity> DatabaseInteractionResult<T> insert(T instance, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		// instantiate and persist a new instance, requires at least identifier field
		T persistence = (T) sessionFactory.unwrap(SessionFactoryImplementor.class).getMetamodel().entityPersister(clazz)
				.instantiate(instance.getId(), session.unwrap(SharedSessionContractImplementor.class));

		session.persist(persistence);

		try {
			// build the persisted entity
			// should always avoid Exceptions here
			GeneBuilder<T> geneBuilder = new CompositeGeneBuilder<>(clazz);

			persistence = geneBuilder.insertion().build(instance);
		} catch (RuntimeException rte) {
			rte.printStackTrace();
			return DatabaseInteractionResult.failed(Map.of("error", rte.getMessage()));
		}

		Specification<T> specification = specificationFactory.getSpecification(clazz);
		// validate the persisted entity
		DatabaseInteractionResult<T> result = specification.isSatisfiedBy(persistence);

		if (result.isOk()) {
			session.save(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	public <T extends Entity> DatabaseInteractionResult<T> update(T model, Class<T> type) {
		Session session = sessionFactory.getCurrentSession();
		// persisted entity should always be persisted here, otherwise, it's either
		// architecture/system fraud or possible thread violation
		T persistence = session.load(type, model.getId());

		try {
			GeneBuilder<T> geneBuilder = new CompositeGeneBuilder<>(type);

			geneBuilder.update().build(model);
		} catch (RuntimeException rte) {
			rte.printStackTrace();
			return DatabaseInteractionResult.failed(Map.of("error", rte.getMessage()));
		}

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
