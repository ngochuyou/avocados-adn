/**
 * 
 */
package adn.dao.specific;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.dao.generic.GenericRepositoryImpl;
import adn.helpers.HibernateHelper;
import adn.model.entities.ProductCost;
import adn.model.entities.Provider;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ProductProviderDetailRepository {

	private static final Logger logger = LoggerFactory.getLogger(ProductProviderDetailRepository.class);

	private final SessionFactory sessionFactory;
	private final GenericRepositoryImpl genericRepository;

	@Autowired
	public ProductProviderDetailRepository(GenericRepositoryImpl genericRepository, SessionFactory sessionFactory) {
		super();
		this.genericRepository = genericRepository;
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("serial")
	public Optional<ProductCost> findCurrent(UUID providerId, BigInteger productId) {
		return genericRepository.findOne(ProductCost.class, new Specification<ProductCost>() {
			@Override
			public Predicate toPredicate(Root<ProductCost> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				return builder.and(hasId(root, builder, providerId, productId), isCurrent(root, builder));
			}
		});
	}

	@SuppressWarnings("serial")
	public Optional<ProductCost> findUnapproved(UUID providerId, BigInteger productId) {
		return genericRepository.findOne(ProductCost.class, new Specification<ProductCost>() {
			@Override
			public Predicate toPredicate(Root<ProductCost> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				return builder.and(hasId(root, builder, providerId, productId), isUnapproved(root, builder));
			}
		});
	}

	@SuppressWarnings("serial")
	public List<Object[]> findAllCurrentByProvider(UUID providerId, Collection<String> columns, Pageable paging) {
		return genericRepository.findAll(ProductCost.class, columns,
				new Specification<ProductCost>() {
					@Override
					public Predicate toPredicate(Root<ProductCost> root, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						return builder.and(hasProviderId(root, builder, providerId), isCurrent(root, builder));
					}
				}, paging);
	}

	@SuppressWarnings("serial")
	public boolean hasUnapproved(UUID providerId, BigInteger productId) {
		return genericRepository.count(ProductCost.class, new Specification<ProductCost>() {
			@Override
			public Predicate toPredicate(Root<ProductCost> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				return builder.and(hasId(root, builder, providerId, productId), isUnapproved(root, builder));
			}
		}) != 0;
	}

	@SuppressWarnings("serial")
	public List<Object[]> findAllCurrentByProduct(String productId, Collection<String> columns, Pageable paging) {
		return genericRepository.findAll(ProductCost.class, columns,
				new Specification<ProductCost>() {
					@Override
					public Predicate toPredicate(Root<ProductCost> root, CriteriaQuery<?> query,
							CriteriaBuilder builder) {
						return builder.and(hasProductId(root, builder, productId), isCurrent(root, builder));
					}
				}, paging);
	}

	public List<Object[]> findAllCurrentProviderOfProduct(String productId, Collection<String> columns,
			Pageable paging) {
		// Provider root
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = builder.createTupleQuery();
		Root<Provider> providerRoot = query.from(Provider.class);
		// ProductProviderDetail root
		Subquery<UUID> subQuery = query.subquery(UUID.class);
		Root<ProductCost> detailRoot = subQuery.from(ProductCost.class);
		// @formatter:off
		subQuery.select(detailRoot.get(_ProductCost.id).get(_ProductCost.providerId));
		subQuery.where(builder.and(
			hasProductId(detailRoot, builder, productId),
			isCurrent(detailRoot, builder)
		));
		// the above query will always return distinct Provider IDs, otherwise, business logic
		// was violated
		// @formatter:on
		query.multiselect(genericRepository.resolveSelect(Provider.class, providerRoot, columns));
		query.where(builder.in(providerRoot.get(_Provider.id)).value(subQuery));
//		query.where(providerRoot.get(_Provider.id).in(subQuery));

		Query<Tuple> hql = genericRepository.resolvePagedQuery(session, query, paging);

		if (logger.isDebugEnabled()) {
			logger.debug(hql.getQueryString());
		}

		return HibernateHelper.toRows(hql.list());
	}

	public static Predicate hasId(Root<ProductCost> root, CriteriaBuilder builder, UUID providerId,
			BigInteger productId) {
		Path<Object> idPath = root.get(_ProductCost.id);
		// @formatter:off
		return builder.and(
				builder.equal(idPath.get(_ProductCost.productId), productId),
				builder.equal(idPath.get(_ProductCost.providerId), providerId));
		// @formatter:on
	}

	public static Predicate hasProviderId(Root<ProductCost> root, CriteriaBuilder builder, UUID providerId) {
		return builder.and(
				builder.equal(root.get(_ProductCost.id).get(_ProductCost.providerId), providerId));
	}

	public static Predicate hasProductId(Root<ProductCost> root, CriteriaBuilder builder, String productId) {
		return builder.and(
				builder.equal(root.get(_ProductCost.id).get(_ProductCost.productId), productId));
	}

	public static Predicate isCurrent(Root<ProductCost> root, CriteriaBuilder builder) {
		// @formatter:off
		return builder.and(
				builder.isNotNull(root.get(_ProductCost.approvedTimestamp)),
				builder.isNull(root.get(_ProductCost.droppedTimestamp)));
		// @formatter:on
	}

	public static Predicate isUnapproved(Root<ProductCost> root, CriteriaBuilder builder) {
		return builder.isNull(root.get(_ProductCost.approvedTimestamp));
	}

}
