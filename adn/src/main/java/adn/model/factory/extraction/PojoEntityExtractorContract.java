/**
 * 
 */
package adn.model.factory.extraction;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class PojoEntityExtractorContract<T extends DomainEntity, M extends DomainEntity>
		implements PojoEntityExtractor<T, M> {

	@Override
	public <E extends T, N extends M> PojoEntityExtractor<E, N> and(PojoEntityExtractor<E, N> next) {
		return new CompositePojoEntityExtractorImplementor<>(this, next);
	}

	@Override
	public final <E extends T, N extends M> E extract(N model) {
		throw new UnsupportedOperationException();
	}

	private class CompositePojoEntityExtractorImplementor<X extends T, Y extends M>
			extends PojoEntityExtractorContract<X, Y> {

		private final PojoEntityExtractor<T, M> first;
		private final PojoEntityExtractor<X, Y> next;

		public CompositePojoEntityExtractorImplementor(PojoEntityExtractor<T, M> first,
				PojoEntityExtractor<X, Y> next) {
			super();
			this.first = first;
			this.next = next;
		}

		@Override
		public <E extends X, N extends Y> E extract(N source, E target) {
			return next.extract(source, first.extract(source));
		}

		@Override
		public String getLoggableName() {
			return String.format("[%s, %s]", first.getLoggableName(), next.getLoggableName());
		}

	}

}
