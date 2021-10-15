/**
 * 
 */
package adn.dao.specific;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionContract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import adn.application.Result;
import adn.dao.generic.GenericRepository;
import adn.model.entities.Item;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.metadata._Item;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ItemRepository {

	private final GenericRepository genericRepository;
	private final SessionFactory sessionFactory;

	@Autowired
	public ItemRepository(GenericRepository genericRepository, SessionFactory sessionFactory) {
		super();
		this.genericRepository = genericRepository;
		this.sessionFactory = sessionFactory;
	}

	public Result<Integer> updateItemsStatus(List<BigInteger> itemIds, ItemStatus status) {
		return updateItemsStatus(itemIds, status, sessionFactory.getCurrentSession());
	}

	public Result<Integer> updateItemsStatus(List<BigInteger> itemIds, ItemStatus status,
			SharedSessionContract session) {
		return genericRepository.update(Item.class, (root, query, builder) -> query.set(root.get(_Item.status), status),
				(root, query, builder) -> builder.in(root.get(_Item.id)).value(itemIds), session);
	}

}
