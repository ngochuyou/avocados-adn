/**
 * 
 */
package adn.service.entity.builder;

import java.io.Serializable;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractCompositeEntityBuilder<T extends Entity> implements EntityBuilder<T> {

	@Override
	public <E extends T> EntityBuilder<E> and(EntityBuilder<E> next) {
		return new CompositeEntityBuilderImpl<>(this, next);
	}

	@Override
	public String getLoggableName() {
		return this.getClass().getSimpleName();
	}

	private class CompositeEntityBuilderImpl<X extends T> extends AbstractCompositeEntityBuilder<X> {

		private final EntityBuilder<T> first;
		private final EntityBuilder<X> next;

		public CompositeEntityBuilderImpl(EntityBuilder<T> first, EntityBuilder<X> next) {
			super();
			this.first = first;
			this.next = next;
		}

		@Override
		public <E extends X> E buildInsertion(Serializable id, E model) {
			return next.buildInsertion(id, first.buildInsertion(id, model));
		}

		@Override
		public <E extends X> E buildUpdate(Serializable id, E model, E persistence) {
			return next.buildUpdate(id, model, first.buildUpdate(id, model, persistence));
		}
		
		@Override
		public <E extends X> E buildPostValidationOnInsert(Serializable id, E model) {
			return next.buildPostValidationOnInsert(id, first.buildPostValidationOnInsert(id, model));
		}

		@Override
		public String getLoggableName() {
			return String.format("[%s, %s]", first.getLoggableName(), next.getLoggableName());
		}

	}

}