/**
 * 
 */
package adn.dao.generic;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import adn.model.entities.Entity;
import adn.model.specification.SpecificationFactory;

/**
 * @author Ngoc Huy
 *
 */
@org.springframework.stereotype.Repository
@Primary
public class BaseRepository extends AbstractRepository implements Repository {

	@Autowired
	public BaseRepository(SessionFactory sessionFactory, SpecificationFactory specificationFactory) {
		super(sessionFactory, specificationFactory);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> insert(Serializable id, E persistence, Class<E> type) {
		return insert(getCurrentSession(), id, persistence, type);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> insert(Session session, Serializable id, E persistence,
			Class<E> type) {
		// validate the persisted entity
		Result<E> result = validate(session, id, persistence, type);

		if (result.isOk()) {
			session.save(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Serializable id, E persistence, Class<E> type) {
		return update(getCurrentSession(), id, persistence, type);
	}

	@Override
	public <T extends Entity, E extends T> Result<E> update(Session session, Serializable id, E persistence,
			Class<E> type) {
		Result<E> result = validate(session, id, persistence, type);

		if (result.isOk()) {
			session.update(persistence);

			return result;
		}

		session.evict(persistence);

		return result;
	}

}
