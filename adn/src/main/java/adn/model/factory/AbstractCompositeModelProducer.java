/**
 * 
 */
package adn.model.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import adn.model.AbstractModel;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractCompositeModelProducer<T extends AbstractModel> implements CompositeModelProducer<T> {

	@Override
	public <E extends T> CompositeModelProducer<E> and(CompositeModelProducer<E> next) {
		return new And<>(this, next);
	}

	private class And<E extends T> extends AbstractCompositeModelProducer<E> {

		private final CompositeModelProducer<T> parent;
		private final CompositeModelProducer<E> child;

		public And(CompositeModelProducer<T> parent, CompositeModelProducer<E> child) {
			super();
			this.parent = parent;
			this.child = child;
		}

		@Override
		public Map<String, Object> produce(E entity) {
			Map<String, Object> parentModel = parent.produce(entity);
			Map<String, Object> childModel = child.produce(entity);

			for (String key : childModel.keySet()) {
				// always override parent's value
				parentModel.merge(key, childModel.get(key), null);
			}

			return parentModel;
		}

		@Override
		public Map<String, Object> produceImmutable(E entity) {
			Map<String, Object> parentModel = parent.produce(entity);
			Map<String, Object> childModel = child.produce(entity);
			Map<String, Object> model = new HashMap<>(parentModel.size() + childModel.size(), 1.0975f);

			model.putAll(parentModel);
			model.putAll(childModel);

			return Collections.unmodifiableMap(model);
		}

		@Override
		public Map<String, Object> produce(E entity, Map<String, Object> modelMap) {
			return child.produce(entity, parent.produce(entity));
		}

		@Override
		public Map<String, Object> produceImmutable(E entity, Map<String, Object> modelMap) {
			return child.produceImmutable(entity, parent.produceImmutable(entity));
		}

	}
}
