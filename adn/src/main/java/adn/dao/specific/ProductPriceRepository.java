/**
 * 
 */
package adn.dao.specific;

import static adn.application.context.ContextProvider.getCurrentSession;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.dao.generic.GenericRepository;
import adn.dao.specific.AbstractSpannedResourceRepository.AbstractLocalDateTimeSpannedResourceRepository;
import adn.model.entities.ProductPrice;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ProductPriceRepository extends AbstractLocalDateTimeSpannedResourceRepository<ProductPrice> {

	private static final Specification<ProductPrice> PRICE_IS_APPROVED = (root, query, builder) -> builder
			.isNotNull(root.get(_ProductPrice.approvalInformations).get(_ProductPrice.approvedTimestamp));

	public ProductPriceRepository(GenericRepository genericRepository) {
		super(ProductPrice.class, genericRepository,
				(root) -> root.get(_ProductPrice.id).get(_ProductPrice.appliedTimestamp),
				(root) -> root.get(_ProductPrice.id).get(_ProductPrice.droppedTimestamp));
	}

	public List<Object[]> findAllCurrents(Collection<BigInteger> productIds, Collection<String> columns) {
		return findAllCurrents(productIds, columns, getCurrentSession());
	}

	public List<Object[]> findAllCurrents(Collection<BigInteger> productIds, Collection<String> columns,
			Session session) {
		return findAllCurrents(columns, (root, query, builder) -> builder
				.in(root.get(_ProductPrice.id).get(_ProductPrice.productId)).value(productIds), session);
	}

	public Optional<Object[]> findOverlapping(BigInteger productId, LocalDateTime appliedTimestamp,
			LocalDateTime droppedTimestamp, Collection<String> columns) {
		return findOverlapping(productId, appliedTimestamp, droppedTimestamp, columns, getCurrentSession());
	}

	public Optional<Object[]> findOverlapping(BigInteger productId, LocalDateTime appliedTimestamp,
			LocalDateTime droppedTimestamp, Collection<String> columns, Session session) {
		// @formatter:off
		return findOverlapping(
				columns,
				(root, query, builder) -> builder.equal(root.get(_ProductPrice.id).get(_ProductPrice.productId), productId),
				appliedTimestamp, droppedTimestamp, session);
		// @formatter:on
	}

	@Override
	protected Specification<ProductPrice> getCurrentSpecification() {
		return PRICE_IS_APPROVED.and(super.getCurrentSpecification());
	}

}
