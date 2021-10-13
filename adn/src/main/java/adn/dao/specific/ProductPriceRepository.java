/**
 * 
 */
package adn.dao.specific;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

	public ProductPriceRepository(GenericRepository genericRepository) {
		super(ProductPrice.class, genericRepository,
				(root) -> root.get(_ProductPrice.id).get(_ProductPrice.appliedTimestamp),
				(root) -> root.get(_ProductPrice.id).get(_ProductPrice.droppedTimestamp));
	}

	public List<Object[]> findAllCurrents(Collection<BigInteger> productIds, Collection<String> columns) {
		return findAllCurrents(columns, (root, query, builder) -> builder
				.in(root.get(_ProductPrice.id).get(_ProductPrice.productId)).value(productIds));
	}

	public Optional<Object[]> findOverlapping(BigInteger productId, LocalDateTime appliedTimestamp,
			LocalDateTime droppedTimestamp, Collection<String> columns) {
		// @formatter:off
		return findOverlapping(
				columns,
				(root, query, builder) -> builder.equal(root.get(_ProductPrice.id).get(_ProductPrice.productId), productId),
				appliedTimestamp, droppedTimestamp);
		// @formatter:on
	}

}
