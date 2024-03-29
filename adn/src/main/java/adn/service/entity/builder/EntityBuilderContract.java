/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import org.hibernate.Session;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class EntityBuilderContract<T extends Entity> implements EntityBuilder<T> {

	@Override
	public <E extends T> EntityBuilder<E> and(EntityBuilder<E> next) {
		return new CompositeEntityBuilder<>(this, next);
	}

	@Override
	public String getLoggableName() {
		return this.getClass().getSimpleName();
	}

	private class CompositeEntityBuilder<X extends T> extends EntityBuilderContract<X> {

		private final EntityBuilder<T> first;
		private final EntityBuilder<X> next;

		public CompositeEntityBuilder(EntityBuilder<T> first, EntityBuilder<X> next) {
			super();
			this.first = first;
			this.next = next;
		}

		@Override
		public String getLoggableName() {
			return String.format("[%s, %s]", first.getLoggableName(), next.getLoggableName());
		}

		@Override
		public <E extends X> E buildInsertion(Serializable id, E model, Session session) {
			return next.buildInsertion(id, first.buildInsertion(id, model, session), session);
		}

		@Override
		public <E extends X> E buildPostValidationOnInsert(Serializable id, E model, Session session) {
			return next.buildPostValidationOnInsert(id, first.buildPostValidationOnInsert(id, model, session), session);
		}

		@Override
		public <E extends X> E buildUpdate(Serializable id, E model, E persistence, Session session) {
			return next.buildUpdate(id, model, first.buildUpdate(id, model, persistence, session), session);
		}

	}

}