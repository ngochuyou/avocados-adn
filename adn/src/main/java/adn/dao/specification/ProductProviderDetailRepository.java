/**
 * 
 */
package adn.dao.specification;

import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import adn.model.entities.ProductProviderDetail;
import adn.model.entities.metadata._ProductProviderDetail;
import adn.service.specification.GenericJpaSpecificationExecutor;

/**
 * @author Ngoc Huy
 *
 */
@Repository
public class ProductProviderDetailRepository {

	private final SessionFactory sessionFactory;
	private final GenericJpaSpecificationExecutor specificationExecutor;

	public ProductProviderDetailRepository(SessionFactory sessionFactory,
			GenericJpaSpecificationExecutor specificationExecutor) {
		super();
		this.sessionFactory = sessionFactory;
		this.specificationExecutor = specificationExecutor;
	}

	public ProductProviderDetail findCurrentProductDetail(UUID providerId, String productId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<ProductProviderDetail> criteriaQuery = builder.createQuery(ProductProviderDetail.class);
		Root<ProductProviderDetail> root = criteriaQuery.from(ProductProviderDetail.class);
		// @formatter:off
		criteriaQuery.where(builder.and(
			hasId(root, builder, providerId, productId),
			isCurrent(root, builder)
		));
		// @formatter:on
		return session.createQuery(criteriaQuery).getResultStream().findFirst().orElse(null);
	}

	public ProductProviderDetail findUnapprovedProductDetail(UUID providerId, String productId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaQuery<ProductProviderDetail> criteriaQuery = builder.createQuery(ProductProviderDetail.class);
		Root<ProductProviderDetail> root = criteriaQuery.from(ProductProviderDetail.class);

		criteriaQuery.where(builder.and(hasId(root, builder, providerId, productId)), isUnapproved(root, builder));

		return session.createQuery(criteriaQuery).getResultStream().findFirst().orElse(null);
	}

	@SuppressWarnings("serial")
	public boolean hasUnapprovedDetail(UUID providerId, String productId) {
		return specificationExecutor.count(ProductProviderDetail.class, new Specification<ProductProviderDetail>() {
			@Override
			public Predicate toPredicate(Root<ProductProviderDetail> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				return builder.and(hasId(root, builder, providerId, productId), isUnapproved(root, builder));
			}
		}) != 0;
	}

	public static Predicate hasId(Root<ProductProviderDetail> root, CriteriaBuilder builder, UUID providerId,
			String productId) {
		Path<Object> idPath = root.get(_ProductProviderDetail.id);
		// @formatter:off
		return builder.and(
				builder.equal(idPath.get(_ProductProviderDetail.productId), productId),
				builder.equal(idPath.get(_ProductProviderDetail.providerId), providerId));
		// @formatter:on
	}

	public static Predicate isCurrent(Root<ProductProviderDetail> root, CriteriaBuilder builder) {
		// @formatter:off
		return builder.and(
				builder.isNotNull(root.get(_ProductProviderDetail.approvedTimestamp)),
				builder.isNull(root.get(_ProductProviderDetail.droppedTimestamp)));
		// @formatter:on
	}

	public static Predicate isUnapproved(Root<ProductProviderDetail> root, CriteriaBuilder builder) {
		return builder.isNull(root.get(_ProductProviderDetail.approvedTimestamp));
	}

}
