/**
 * 
 */
package adn.dao;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import adn.model.DatabaseInteractionResult;
import adn.model.entities.Entity;
import adn.model.specification.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Repository
@Primary
public class BaseRepository extends AbstractRepository<Entity> implements Repository<Entity> {

	@Autowired
	public BaseRepository(SessionFactory sessionFactory, SpecificationFactory specificationFactory) {
		super(sessionFactory, specificationFactory);
	}

	@Override
	public <E extends Entity> DatabaseInteractionResult<E> insert(Serializable id, E persistence, Class<E> type) {
		Session session = sessionFactory.getCurrentSession();
		// validate the persisted entity
		DatabaseInteractionResult<E> result = validate(id, persistence, type);

		if (result.isOk()) {
			session.save(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	@Override
	public <E extends Entity> DatabaseInteractionResult<E> update(Serializable id, E persistence, Class<E> type) {
		Session session = sessionFactory.getCurrentSession();
		DatabaseInteractionResult<E> result = validate(id, persistence, type);

		if (result.isOk()) {
			session.update(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

}
