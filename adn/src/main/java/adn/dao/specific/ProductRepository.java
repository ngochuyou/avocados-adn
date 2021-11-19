/**
 * 
 */
package adn.dao.specific;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.application.context.ContextProvider;
import adn.controller.query.filter.BigDecimalFilter;
import adn.controller.query.filter.StringFilter;
import adn.controller.query.impl.ProductQuery;
import adn.dao.generic.GenericRepositoryImpl;
import adn.helpers.HibernateHelper;
import adn.model.entities.Item;
import adn.model.entities.Product;
import adn.model.entities.ProductPrice;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ProductRepository {

	private static final Logger logger = LoggerFactory.getLogger(ProductRepository.class);

	private final GenericRepositoryImpl genericRepository;

	public ProductRepository(GenericRepositoryImpl genericRepository) {
		super();
		this.genericRepository = genericRepository;
	}

	public List<Object[]> findOnSaleProducts(Collection<String> columns, Pageable paging, ProductQuery restQuery) {
		return findOnSaleProducts(columns, paging, null, restQuery);
	}

	public List<Object[]> findOnSaleProducts(Collection<String> columns, Pageable paging, Specification<Product> spec,
			ProductQuery restQuery) {
		Session session = ContextProvider.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<Product> root = query.from(Product.class);

		query.multiselect(genericRepository.resolveSelect(Product.class, root, columns));

		Join<Product, ProductPrice> priceJoin = root.join(_Product.prices);
		Path<Object> priceId = priceJoin.get(_ProductPrice.id);
		// @formatter:off
		BigDecimalFilter priceFilter = restQuery.getPrice();
		StringFilter nameFilter = restQuery.getName();
		
		query.where(builder.and(genericRepository.resolvePredicate(Product.class, root, query, builder, spec),
				builder.and(
					builder.equal(root.get(_Product.locked), false),
					builder.isNotNull(
							priceJoin.get(_ProductPrice.approvalInformations).get(_ProductPrice.approvedTimestamp)),
					builder.between(builder.literal(LocalDateTime.now()),
							priceId.get(_ProductPrice.appliedTimestamp),
							priceId.get(_ProductPrice.droppedTimestamp)),
					nameFilter != null ?
							builder.like(root.get(_Product.name), nameFilter.getLike()) :
								builder.conjunction(),
					priceFilter != null ?
							builder.and(
									Optional.ofNullable(priceFilter.getFrom())
										.map(from -> builder.greaterThanOrEqualTo(priceJoin.get(_ProductPrice.price), from)).orElse(builder.conjunction()),
									Optional.ofNullable(priceFilter.getTo())
										.map(to -> builder.lessThanOrEqualTo(priceJoin.get(_ProductPrice.price), to)).orElse(builder.conjunction()))
							: builder.conjunction())));
		// @formatter:on
		Order priceOrder = paging.getSort().getOrderFor(_ProductPrice.price);

		if (priceOrder != null) {
			query.orderBy(priceOrder.getDirection() == Direction.ASC ? builder.asc(priceJoin.get(_ProductPrice.price))
					: builder.desc(priceJoin.get(_ProductPrice.price)));
		}

		Query<Tuple> hql = genericRepository.resolvePagedQuery(session, query, paging);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return HibernateHelper.toRows(hql.list());
	}

	public List<Object[]> findItemsByProduct(BigInteger productId, Collection<String> columns, Specification<Item> spec,
			boolean doCount) {
		Specification<Item> finalSpec = (root, query, builder) -> builder
				.equal(root.get(_Item.product).get(_Product.id), productId);

		finalSpec = finalSpec.and(spec);

		if (!doCount) {
			return genericRepository.findAll(Item.class, columns, finalSpec);
		}

		Session session = ContextProvider.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<Item> root = query.from(Item.class);

		List<Selection<?>> selections = genericRepository.resolveSelect(Item.class, root, columns);

		selections.add(builder.count(root));
		query.multiselect(selections)
				.where(builder.and(genericRepository.resolvePredicate(Item.class, root, query, builder, finalSpec)));
		query.groupBy(root.get(_Item.namedSize), root.get(_Item.color));

		Query<Tuple> hql = session.createQuery(query);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return HibernateHelper.toRows(hql.list());
	}

}
