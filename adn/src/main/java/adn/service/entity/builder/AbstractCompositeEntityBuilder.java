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
		public <E extends X> E buildInsertion(Serializable id, E entity) {
			return next.buildInsertion(id, first.buildInsertion(id, entity));
		}

		@Override
		public <E extends X> E buildUpdate(Serializable id, E entity, E persistence) {
			return next.buildUpdate(id, entity, first.buildUpdate(id, entity, persistence));
		}

		@Override
		public String getLoggableName() {
			return String.format("[%s, %s]", first.getLoggableName(), next.getLoggableName());
		}

	}

}