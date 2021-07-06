/**
 * 
 */
package adn.model.factory.dictionary.production;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adn.model.AbstractModel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractCompositeDictionaryAuthenticationBasedModelProducer<T extends AbstractModel>
		implements CompositeDictionaryAuthenticationBasedModelProducer<T> {

	@Override
	public <E extends T> CompositeDictionaryAuthenticationBasedModelProducer<E> and(
			CompositeDictionaryAuthenticationBasedModelProducer<E> next) {
		return new And<>(this, next);
	}

	@SuppressWarnings("unchecked")
	private class And<E extends T> extends AbstractCompositeDictionaryAuthenticationBasedModelProducer<E> {

		private final CompositeDictionaryAuthenticationBasedModelProducer<T> parent;
		private final CompositeDictionaryAuthenticationBasedModelProducer<E> child;

		public And(CompositeDictionaryAuthenticationBasedModelProducer<T> parent,
				CompositeDictionaryAuthenticationBasedModelProducer<E> child) {
			this.parent = parent;
			this.child = child;
		}

		@Override
		public Map<String, Object> produce(E entity, Role role) {
			Map<String, Object> parentModel = parent.produce(entity, role);
			Map<String, Object> childModel = child.produce(entity, role);

			for (String key : childModel.keySet()) {
				parentModel.merge(key, childModel.get(key), null);
			}

			return parentModel;
		}

		@Override
		public Map<String, Object> produceImmutable(E entity, Role role) {
			Map<String, Object> parentModel = parent.produceImmutable(entity, role);
			Map<String, Object> childModel = child.produceImmutable(entity, role);
			Map<String, Object> finalModel = new HashMap<>(parentModel.size() + childModel.size(), 1.0975f);

			finalModel.putAll(parentModel);
			finalModel.putAll(childModel);

			return Collections.unmodifiableMap(finalModel);
		}

		@Override
		public Map<String, Object> produce(E entity, Map<String, Object> modelMap, Role role) {
			return child.produce(entity, parent.produce(entity, modelMap, role), role);
		}

		@Override
		public Map<String, Object> produceImmutable(E entity, Map<String, Object> modelMap, Role role) {
			return immutablyJoin(parent.produceImmutable(entity, modelMap, role),
					child.produceImmutable(entity, modelMap, role));
		}

		@Override
		public Map<String, Object> produce(E source, Map<String, Object> model) {
			return child.produce(source, parent.produce(source, model, null), null);
		}

		@Override
		public Map<String, Object> produceImmutable(E source, Map<String, Object> model) {
			return immutablyJoin(parent.produceImmutable(source, model), child.produceImmutable(source, model));
		}

		@Override
		public Map<String, Object> produce(E source) {
			return join(parent.produce(source), child.produce(source));
		}

		@Override
		public Map<String, Object> produceImmutable(E source) {
			return immutablyJoin(parent.produceImmutable(source), child.produceImmutable(source));
		}

		private Map<String, Object> join(Map<String, Object> left, Map<String, Object> right) {
			Map<String, Object> finalModel = new HashMap<>(left.size() + right.size(), 1.075f);

			finalModel.putAll(left);
			finalModel.putAll(right);

			return finalModel;
		}

		private Map<String, Object> immutablyJoin(Map<String, Object> left, Map<String, Object> right) {
			return Collections.unmodifiableMap(join(left, right));
		}

		@Override
		public String getName() {
			return String.format("[%s, %s]", parent.getName(), child.getName());
		}

		@Override
		public List<Map<String, Object>> produce(List<E> source, List<Map<String, Object>> models, Role role) {
			return child.produce(source, parent.produce((List<T>) source, models, role), role);
		}

		@Override
		public List<Map<String, Object>> produceImmutable(List<E> source, List<Map<String, Object>> models, Role role) {
			return immutablyJoin(parent.produceImmutable((List<T>) source, models, role),
					child.produceImmutable(source, models, role));
		}

		@Override
		public List<Map<String, Object>> produce(List<E> source, Role role) {
			return join(parent.produce((List<T>) source, role), child.produce(source, role));
		}

		@Override
		public List<Map<String, Object>> produceImmutable(List<E> source, Role role) {
			return immutablyJoin(parent.produce((List<T>) source, role), child.produce(source, role));
		}

		@Override
		public List<Map<String, Object>> produce(List<E> sources, List<Map<String, Object>> models) {
			return child.produce(sources, parent.produce((List<T>) sources, models));
		}

		@Override
		public List<Map<String, Object>> produceImmutable(List<E> sources, List<Map<String, Object>> models) {
			return immutablyJoin(parent.produceImmutable((List<T>) sources, models),
					child.produceImmutable(sources, models));
		}

		@Override
		public List<Map<String, Object>> produce(List<E> sources) {
			return join(parent.produce((List<T>) sources), child.produce(sources));
		}

		@Override
		public List<Map<String, Object>> produceImmutable(List<E> sources) {
			return immutablyJoin(parent.produceImmutable((List<T>) sources), child.produceImmutable(sources));
		}

		private List<Map<String, Object>> join(List<Map<String, Object>> left, List<Map<String, Object>> right) {
			List<Map<String, Object>> finalProds = new ArrayList<>(left.size() + right.size());

			finalProds.addAll(left);
			finalProds.addAll(right);

			return finalProds;
		}

		private List<Map<String, Object>> immutablyJoin(List<Map<String, Object>> left,
				List<Map<String, Object>> right) {
			return Collections.unmodifiableList(join(left, right));
		}

	}
}