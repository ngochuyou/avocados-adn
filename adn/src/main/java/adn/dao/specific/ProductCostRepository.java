/**
 * 
 */
package adn.dao.specific;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.application.Common;
import adn.dao.generic.GenericRepository;
import adn.dao.specific.AbstractSpannedResourceRepository.AbstractLocalDateTimeSpannedResourceRepository;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.helpers.Utils.Wrapper;
import adn.model.entities.ProductCost;
import adn.model.entities.id.ProductCostId;
import adn.model.entities.metadata._ProductCost;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ProductCostRepository extends AbstractLocalDateTimeSpannedResourceRepository<ProductCost> {

	private static final String IN_TEMPLATE;
	private static final String ID_PAIR_PARAMETER = "(?%d, ?%d)";

	static {
		// @formatter:off
		IN_TEMPLATE = String.format(
				"(%s, %s) IN (%s)",
				StringHelper.join(Common.DOT, _ProductCost.id, _ProductCost.productId),
				StringHelper.join(Common.DOT, _ProductCost.id, _ProductCost.providerId),
				"%s");
		// @formatter:on
	}

	// @formatter:off
	@Autowired
	public ProductCostRepository(GenericRepository genericRepository) {
		super(ProductCost.class,
				genericRepository,
				(root) -> root.get(_ProductCost.id).get(_ProductCost.appliedTimestamp),
				(root) -> root.get(_ProductCost.id).get(_ProductCost.droppedTimestamp));
	}
	// @formatter:on

	public Optional<Object[]> findCurrent(UUID providerId, BigInteger productId, Collection<String> columns) {
		return findCurrent(columns, hasId(productId, providerId));
	}

	public List<Object[]> findAllCurrents(Collection<Utils.Entry<BigInteger, UUID>> idPairs,
			Collection<String> columns) {
		StringBuilder inClauseParametersBuilder = new StringBuilder();
		Wrapper<Integer> indexWrapper = new Wrapper<Integer>(0);
		Object[] parameters = idPairs.stream().flatMap(pair -> {
			inClauseParametersBuilder
					.append(getPairParameterString(indexWrapper.getThenMap(index -> index + 1)) + Common.COMMA);
			return Stream.of(pair.getKey(), pair.getValue());
		}).toArray();
		// @formatter:off
		return findAllCurrents(
				columns,
				String.format(
						IN_TEMPLATE,
						inClauseParametersBuilder.deleteCharAt(inClauseParametersBuilder.length() - 1)),
				parameters);
		// @formatter:on
	}

	private String getPairParameterString(int currentIndex) {
		int position = currentIndex * 2 + 1;
		return String.format(ID_PAIR_PARAMETER, position++, position);
	}

	public Optional<Object[]> findOverlapping(UUID providerId, BigInteger productId, LocalDateTime appliedTimestamp,
			LocalDateTime droppedTimestamp, Collection<String> columns) {
		return findOverlapping(columns, hasId(productId, providerId), appliedTimestamp, droppedTimestamp);
	}

	private interface IdentifierPredicateResolver {

		Predicate resolve(Path<ProductCostId> idPath, CriteriaBuilder builder, Serializable memberValue);

	}

	private static final List<IdentifierPredicateResolver> ID_PREDICATE_RESOLVERS = List.of(
			(path, builder, memberValue) -> builder.equal(path.get(_ProductCost.productId), memberValue),
			(path, builder, memberValue) -> builder.equal(path.get(_ProductCost.providerId), memberValue),
			(path, builder, memberValue) -> builder.equal(path.get(_ProductCost.appliedTimestamp), memberValue),
			(path, builder, memberValue) -> builder.equal(path.get(_ProductCost.droppedTimestamp), memberValue));

	private Specification<ProductCost> hasId(Serializable... identifierMembers) {
		return hasId(0, identifierMembers);
	}

	private Specification<ProductCost> hasId(Integer offset, Serializable... members) {
		return (root, query, builder) -> {
			Path<ProductCostId> idPath = root.get(_ProductCost.id);

			return builder.and(IntStream.range(offset, members.length)
					.mapToObj(index -> ID_PREDICATE_RESOLVERS.get(index).resolve(idPath, builder, members[index]))
					.toArray(Predicate[]::new));
		};
	}

	public List<Object[]> findAllCurrentsByProducts(Collection<BigInteger> productIds, Collection<String> columns) {
		return findAllCurrents(columns, (root, query, builder) -> builder
				.in(root.get(_ProductCost.id).get(_ProductCost.productId)).value(productIds));
	}

//	public List<Object[]> findAllCurrents(Collection<BigInteger> productIds, Collection<UUID> providerIds, Collection<String> columns) {
//		return findAllCurrents(columns, (root, query, builder) -> builder);
//	}

	public List<Object[]> findAllCurrentByProvider(UUID providerId, Collection<String> columns, Pageable paging) {
//		return genericRepository.findAll(ProductCost.class, columns,
//				(root, query, builder) -> builder.and(hasId(root, builder, 1, providerId), isCurrent(root, builder)),
//				paging);
		return null;
	}

	public List<Object[]> findAllCurrentByProduct(String productId, Collection<String> columns, Pageable paging) {
//		return genericRepository.findAll(ProductCost.class, columns,
//				(root, query, builder) -> builder.and(hasId(root, builder, productId), isCurrent(root, builder)),
//				paging);
		return null;
	}

	public List<Object[]> findAllCurrentProvidersOfProduct(String productId, Collection<String> columns,
			Pageable paging) {
//		// Provider root
//		Session session = sessionFactory.getCurrentSession();
//		CriteriaBuilder builder = session.getCriteriaBuilder();
//		CriteriaQuery<Tuple> query = builder.createTupleQuery();
//		Root<Provider> providerRoot = query.from(Provider.class);
//		// ProductProviderDetail root
//		Subquery<UUID> subQuery = query.subquery(UUID.class);
//		Root<ProductCost> costRoot = subQuery.from(ProductCost.class);
//		// @formatter:off
//		subQuery.select(costRoot.get(_ProductCost.id).get(_ProductCost.providerId));
//		subQuery.where(builder.and(
//			hasId(costRoot, builder, productId),
//			isCurrent(costRoot, builder)
//		));
//		// the above query will always return distinct Provider IDs, otherwise, business logic
//		// was violated
//		// @formatter:on
//		query.multiselect(genericRepository.resolveSelect(Provider.class, providerRoot, columns));
//		query.where(builder.in(providerRoot.get(_Provider.id)).value(subQuery));
////		query.where(providerRoot.get(_Provider.id).in(subQuery));
//
//		Query<Tuple> hql = genericRepository.resolvePagedQuery(session, query, paging);
//
//		if (logger.isDebugEnabled()) {
//			logger.debug(hql.getQueryString());
//		}
//
//		return HibernateHelper.toRows(hql.list());
		return null;
	}

//	public static Predicate isCurrent(Root<ProductCost> root, CriteriaBuilder builder) {
//		// @formatter:off
//		return builder.and(
//				builder.isNotNull(root.get(_ProductCost.approvedTimestamp)),
//				builder.isNull(root.get(_ProductCost.droppedTimestamp)));
//		// @formatter:on
//	}

}
