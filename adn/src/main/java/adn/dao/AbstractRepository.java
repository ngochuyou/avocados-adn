/**
 * 
 */
package adn.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;
import adn.model.specification.Specification;
import adn.model.specification.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractRepository<T extends Entity> implements Repository<T> {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final SessionFactory sessionFactory;
	protected final SpecificationFactory specificationFactory;

	public AbstractRepository(final SessionFactory sessionFactory, final SpecificationFactory specificationFactory) {
		this.sessionFactory = sessionFactory;
		this.specificationFactory = specificationFactory;
	}

	@Override
	public <E extends T> E findById(Serializable id, Class<E> clazz) {
		return sessionFactory.getCurrentSession().get(clazz, id);
	}

	@Override
	public <E extends T> E findOne(CriteriaQuery<E> query, Class<E> clazz) {
		// @formatter:off
		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultStream().findFirst().orElse(null);
		// @formatter:on
	}

	@Override
	public <E extends T> List<E> find(CriteriaQuery<E> query, Class<E> clazz) {
		// @formatter:off
		return sessionFactory.getCurrentSession()
				.createQuery(query)
				.getResultList();
		// @formatter:on
	}

	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	protected <E extends T> Specification<E> getSpecification(Class<E> type) {
		return specificationFactory.getSpecification(type);
	}

	protected <E extends T> DatabaseInteractionResult<E> validate(Serializable id, E instance, Class<E> type) {
		Specification<E> spec = getSpecification(type);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Validating [%s#%s] using [%s]", type.getName(), id, spec.getClass().getName()));
		}

		return spec.isSatisfiedBy(id, instance);
	}

}
