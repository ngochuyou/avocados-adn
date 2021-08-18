/**
 * 
 */
package adn.model.specification.generic;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.springframework.stereotype.Component;

import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Factor;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Generic(entityGene = Factor.class)
public class FactorSpecification<T extends Factor> extends EntitySpecification<T> {

	private static final Pattern NAME_PATTERN;
	private static final int MINIMUM_NAME_LENGTH = 1;
	private static final int MAXIMUM_NAME_LENGTH = 255;

	static {
		NAME_PATTERN = Pattern.compile(String.format("^[\\p{L}\\p{N}\\s\\.,_\\-@\"\'%%\\*%s]+$",
				StringHelper.VIETNAMESE_CHARACTERS, MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH));
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, T instance) {
		return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
	}

	@Override
	public Result<T> isSatisfiedBy(Session session, Serializable id, T instance) {
		Result<T> result = super.isSatisfiedBy(session, id, instance);

		if (instance.getName() == null || instance.getName().length() == MINIMUM_NAME_LENGTH
				|| instance.getName().length() > MAXIMUM_NAME_LENGTH) {
			result.bad().getMessages().put("name",
					String.format("Name length must vary between %d and %d", MINIMUM_NAME_LENGTH, MAXIMUM_NAME_LENGTH));
		}

		if (!NAME_PATTERN.matcher(instance.getName()).matches()) {
			result.bad().getMessages().put("name",
					"Name can only contain alphabetic, numeric characters, spaces or '.', ',', '_', '-', @, \", ' and * character");
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
						builder.equal(root.get("name"), instance.getName()),
						id == null ?
							builder.isNotNull(root.get(idPropertyName)) :
								builder.notEqual(root.get(idPropertyName), id)));
			// @formatter:on

			if (session.createQuery(query).getResultStream().findFirst().orElse(0L) != 0) {
				result.bad().getMessages().put("name", "Name was taken");
			}
		}

		if (instance.isActive() == null) {
			result.bad().getMessages().put("active", "Active state must not be empty");
		}

		return result;
	}

}
