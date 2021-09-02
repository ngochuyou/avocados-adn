/**
 * 
 */
package adn.model.entities.specification;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.model.Generic;
import adn.model.entities.Factor;
import adn.model.entities.metadata._Factor;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Factor.class)
public class FactorSpecification<T extends Factor> extends PermanentEntitySpecification<T> {

	private static final String INVALID_NAME = String.format(
			"Name length must vary between %d and %d. Can only contain alphabetic, numeric characters, spaces or '.', ',', '_', '-', @, \\\", ' and '*' character",
			_Factor.MINIMUM_NAME_LENGTH, _Factor.MAXIMUM_NAME_LENGTH);
	private static final String TAKEN_NAME = "Name was taken";
	private static final String EMPTY_DEACTIVATED_TIMESTAMP = "Deactivation timestamp must not be empty when resource is deactivated";
	private static final String INVALID_DEACTIVATED_TIMESTAMP = "Deactivation timestamp must not be later then current time instant";
	private static final String EMPTY_CREATOR = "Creator information must not be empty";
	private static final String EMPTY_UPDATOR = "Updator information must not be empty";
	private static final String EMPTY_APPROVED_TIMESTAMP = "Approval timestamp must not be empty when resource is approved";
	private static final String INVALID_APPROVED_TIMESTAMP = "Approval timestamp must not present when resource is not approved";

	@Override
	public Result<T> isSatisfiedBy(Session session, T instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (!_Factor.NAME_PATTERN.matcher(instance.getName()).matches()) {
			result.bad().getMessages().put(_Factor.name, INVALID_NAME);
		} else {
			Class<? extends T> persistentClass = HibernateHelper.getPersistentClass(instance);
			CriteriaBuilder builder = session.getCriteriaBuilder();
			CriteriaQuery<Long> query = builder.createQuery(Long.class);
			Root<? extends T> root = query.from(persistentClass);
			String idPropertyName = HibernateHelper.getIdentifierPropertyName(persistentClass);
			// UUID needs a different approach to check uniqueness by id
			// @formatter:off
			query
				.select(builder.count(root))
				.where(
					builder.and(
						builder.equal(root.get(_Factor.name), instance.getName()),
						id == null ?
							builder.isNotNull(root.get(idPropertyName)) :
								builder.notEqual(root.get(idPropertyName), id)));
			// @formatter:on
			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				result.bad().getMessages().put(_Factor.name, TAKEN_NAME);
			}
		}

		if (!instance.isActive()) {
			if (instance.getDeactivatedTimestamp() == null) {
				result.bad().getMessages().put(_Factor.active, EMPTY_DEACTIVATED_TIMESTAMP);
			} else {
				if (instance.getDeactivatedTimestamp().isAfter(LocalDateTime.now())) {
					result.bad().getMessages().put(_Factor.active, INVALID_DEACTIVATED_TIMESTAMP);
				}
			}
		}

		if (instance.getCreatedBy() == null) {
			result.bad().getMessages().put(_Factor.createdBy, EMPTY_CREATOR);
		}

		if (instance.getUpdatedBy() == null) {
			result.bad().getMessages().put(_Factor.createdBy, EMPTY_UPDATOR);
		}

		boolean isApproved = instance.getApprovedBy() != null;
		boolean hasApprovedTimestamp = instance.getApprovedTimestamp() != null;

		if (isApproved) {
			if (!hasApprovedTimestamp) {
				result.bad().getMessages().put(_Factor.approvedTimestamp, EMPTY_APPROVED_TIMESTAMP);
			}
		} else {
			if (hasApprovedTimestamp) {
				result.bad().getMessages().put(_Factor.approvedTimestamp, INVALID_APPROVED_TIMESTAMP);
			}
		}

		return result;
	}

}
