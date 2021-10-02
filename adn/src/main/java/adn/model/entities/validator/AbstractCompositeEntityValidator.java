/**
 * 
 */
package adn.model.entities.validator;

import java.io.Serializable;

import org.hibernate.Session;

import adn.dao.generic.Result;
import adn.model.entities.Entity;
import adn.service.internal.Service.Status;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractCompositeEntityValidator<T extends Entity> implements Validator<T> {

	@Override
	public <E extends T> Validator<E> and(Validator<E> next) {
		return new CompositeEntityValidatorImpl<>(this, next);
	}

	private class CompositeEntityValidatorImpl<E extends T> extends AbstractCompositeEntityValidator<E> {

		private final Validator<T> first;
		private final Validator<E> next;

		public CompositeEntityValidatorImpl(Validator<T> first, Validator<E> next) {
			super();
			this.first = first;
			this.next = next;
		}

		@Override
		public Result<E> isSatisfiedBy(Session session, E instance) {
			return join(first.isSatisfiedBy(session, instance), next.isSatisfiedBy(session, instance));
		}

		@Override
		public Result<E> isSatisfiedBy(Session session, Serializable id, E instance) {
			return join(first.isSatisfiedBy(session, id, instance), next.isSatisfiedBy(session, id, instance));
		}

		@Override
		public String getLoggableName() {
			return String.format("[%s, %s]", first.getLoggableName(), next.getLoggableName());
		}

	}

	private static <T extends Entity, E extends T> Result<E> join(Result<T> first, Result<E> next) {
		next.setStatus(first.isOk() && next.isOk() ? Status.OK : Status.BAD);
		next.getMessages().putAll(first.getMessages());

		return next;
	}

}
