/**
 * 
 */
package adn.model.entities.validator;

import static adn.application.Common.notEmpty;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import adn.application.Common;
import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.entities.FullyAuditedEntity;
import adn.model.entities.metadata._FullyAuditedEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractFullyAuditedValidator<T extends FullyAuditedEntity<?>>
		extends AbstractPermanentEntityValidator<T> {

	public static final Pattern NAME_PATTERN;

	static {
		NAME_PATTERN = Pattern.compile(
				String.format("^[%s\\p{L}\\p{N}\\s\\.,_\\-@\"\'%%*]{%d,%d}$", StringHelper.VIETNAMESE_CHARACTERS,
						_FullyAuditedEntity.MINIMUM_NAME_LENGTH, _FullyAuditedEntity.MAXIMUM_NAME_LENGTH));
	}

	private static final String INVALID_NAME = String.format(
			"Name length must vary between %d and %d. Can only contain alphabetic, numeric characters, %s",
			_FullyAuditedEntity.MINIMUM_NAME_LENGTH, _FullyAuditedEntity.MAXIMUM_NAME_LENGTH,
			Common.symbolNamesOf('\s', '.', ',', '_', '-', '@', '\"', '\'', '%', '*'));
	private static final String TAKEN_NAME = "Name was taken";
	private static final String EMPTY_APPROVED_TIMESTAMP = String.format("%s when resource is approved",
			notEmpty("Approval timestamp"));
	private static final String INVALID_APPROVED_TIMESTAMP = String.format("%s while resource hasn't been approved",
			Common.mustEmpty("Approval timestamp"));

	@Override
	public Result<T> isSatisfiedBy(Session session, T instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (!NAME_PATTERN.matcher(instance.getName()).matches()) {
			result.bad().getMessages().put(_FullyAuditedEntity.name, INVALID_NAME);
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
						builder.equal(root.get(_FullyAuditedEntity.name), instance.getName()),
						id == null ?
							builder.isNotNull(root.get(idPropertyName)) :
								builder.notEqual(root.get(idPropertyName), id)));
			// @formatter:on
			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				result.bad().getMessages().put(_FullyAuditedEntity.name, TAKEN_NAME);
			}
		}

		boolean isApproved = instance.getApprovedBy() != null;
		boolean hasApprovedTimestamp = instance.getApprovedTimestamp() != null;

		if (isApproved) {
			if (!hasApprovedTimestamp) {
				result.bad().getMessages().put(_FullyAuditedEntity.approvedTimestamp, EMPTY_APPROVED_TIMESTAMP);
			}
		} else {
			if (hasApprovedTimestamp) {
				result.bad().getMessages().put(_FullyAuditedEntity.approvedTimestamp, INVALID_APPROVED_TIMESTAMP);
			}
		}

		return result;
	}

}
