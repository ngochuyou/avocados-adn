/**
 * 
 */
package adn.dao.specific;

import static adn.application.Common.COMMA;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.LockModeType;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import adn.application.context.ContextProvider;
import adn.dao.generic.GenericRepositoryImpl;
import adn.helpers.HibernateHelper;
import adn.model.entities.Item;
import adn.model.entities.constants.ItemStatus;
import adn.model.entities.metadata._Item;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ItemRepository {

	private static final Logger logger = LoggerFactory.getLogger(ItemRepository.class);

	private final String findItemsAndLockTemplate;

	@Autowired
	public ItemRepository() {
		super();
		// @formatter:off
		findItemsAndLockTemplate = String.format("SELECT %s FROM %s WHERE %s IN (%s)",
				"%s", HibernateHelper.getEntityName(Item.class), _Item.id, "%s");
		// @formatter:on
	}

	public List<Object[]> findAllItemsAndLock(List<BigInteger> itemIds, Collection<String> columns) {
		Session session = ContextProvider.getCurrentSession();
		String hql = String.format(findItemsAndLockTemplate, columns.stream().collect(Collectors.joining(COMMA)),
				IntStream.range(1, itemIds.size() + 1).mapToObj(GenericRepositoryImpl::positionalParam)
						.collect(Collectors.joining(COMMA)));
		Query<Object[]> query = session.createQuery(hql, Object[].class);

		IntStream.range(0, itemIds.size()).forEach(index -> query.setParameter(index + 1, itemIds.get(index)));
		query.setLockMode(LockModeType.PESSIMISTIC_WRITE);

		if (logger.isDebugEnabled()) {
			logger.debug(query.getQueryString());
		}

		return query.list();
	}

	public int updateItemsStatus(List<BigInteger> itemIds, ItemStatus status) {
		Session session = ContextProvider.getCurrentSession();
		String hql = String.format("UPDATE %s SET %s = :%s WHERE %s IN (%s)", HibernateHelper.getEntityName(Item.class),
				_Item.status, _Item.status, _Item.id, IntStream.range(1, itemIds.size() + 1)
						.mapToObj(GenericRepositoryImpl::positionalParam).collect(Collectors.joining(COMMA)));
		Query<?> query = session.createQuery(hql);

		IntStream.range(0, itemIds.size()).forEach(index -> query.setParameter(index + 1, itemIds.get(index)));
		query.setParameter(_Item.status, status);

		return query.executeUpdate();
	}

}
