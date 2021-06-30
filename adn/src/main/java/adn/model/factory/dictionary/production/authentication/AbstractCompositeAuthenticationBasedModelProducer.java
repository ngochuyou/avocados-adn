/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import adn.model.AbstractModel;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractCompositeAuthenticationBasedModelProducer<T extends AbstractModel>
		implements CompositeAuthenticationBasedModelProducer<T> {

	@Override
	public <E extends T> CompositeAuthenticationBasedModelProducer<E> and(
			CompositeAuthenticationBasedModelProducer<E> next) {
		return new And<>(this, next);
	}

	private class And<E extends T> extends AbstractCompositeAuthenticationBasedModelProducer<E> {

		private final CompositeAuthenticationBasedModelProducer<T> parent;
		private final CompositeAuthenticationBasedModelProducer<E> child;

		public And(CompositeAuthenticationBasedModelProducer<T> parent,
				CompositeAuthenticationBasedModelProducer<E> child) {
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
			Map<String, Object> model = new HashMap<>(parentModel.size() + childModel.size(), 1.0975f);

			model.putAll(parentModel);
			model.putAll(childModel);

			return Collections.unmodifiableMap(model);
		}

		@Override
		public Map<String, Object> produce(E entity, Map<String, Object> modelMap, Role role) {
			return child.produce(entity, parent.produce(entity, modelMap, role), role);
		}

		@Override
		public Map<String, Object> produceImmutable(E entity, Map<String, Object> modelMap, Role role) {
			return child.produceImmutable(entity, parent.produceImmutable(entity, modelMap, role), role);
		}

		@Override
		public String getName() {
			return String.format("[%s, %s]", parent.getName(), child.getName());
		}

	}
}
