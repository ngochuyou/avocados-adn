/**
 * 
 */
package adn.model.factory.dictionary.production;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractCompositeDictionaryModelProducer<T extends DomainEntity>
		implements CompositeDictionaryModelProducer<T> {

	@Override
	public <E extends T> CompositeDictionaryModelProducer<E> and(CompositeDictionaryModelProducer<E> next) {
		return new And<>(this, next);
	}

	private class And<E extends T> extends AbstractCompositeDictionaryModelProducer<E> {

		private final CompositeDictionaryModelProducer<T> parent;
		private final CompositeDictionaryModelProducer<E> child;

		public And(CompositeDictionaryModelProducer<T> parent, CompositeDictionaryModelProducer<E> child) {
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
		public Map<String, Object> produce(E entity, Map<String, Object> modelMap) {
			return child.produce(entity, parent.produce(entity));
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Map<String, Object>> produce(List<E> sources, List<Map<String, Object>> models) {
			return child.produce(sources, parent.produce((List<T>) sources, models));
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<Map<String, Object>> produce(List<E> sources) {
			return join(parent.produce((List<T>) sources), child.produce(sources));
		}

		private List<Map<String, Object>> join(List<Map<String, Object>> left, List<Map<String, Object>> right) {
			List<Map<String, Object>> finalProds = new ArrayList<>(left.size() + right.size());

			finalProds.addAll(left);
			finalProds.addAll(right);

			return finalProds;
		}

	}
}
