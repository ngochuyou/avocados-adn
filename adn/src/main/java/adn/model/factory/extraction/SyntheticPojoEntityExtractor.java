/**
 * 
 */
package adn.model.factory.extraction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.PojoInstantiator;

import adn.engine.access.PropertyAccessStrategyFactory;
import adn.engine.access.StandardAccess;
import adn.helpers.Utils.Wrapper;
import adn.model.entities.Entity;
import adn.model.models.Model;

/**
 * @author Ngoc Huy
 *
 */
public class SyntheticPojoEntityExtractor<T extends Entity, M extends Model> extends PojoEntityExtractorContract<T, M> {

	private final List<AccessorPair> accessors;

	public SyntheticPojoEntityExtractor(Class<T> entityType, Class<M> modelType) {
		List<AccessorPair> accessors = new ArrayList<>(0);
		MappedTo anno;
		String modelAttributeName;
		String[] entityAttributeNames;

		for (Field modelField : modelType.getDeclaredFields()) {
			modelAttributeName = modelField.getName();
			anno = modelField.getDeclaredAnnotation(MappedTo.class);
			entityAttributeNames = anno == null || anno.value().length == 0 ? new String[] { modelAttributeName }
					: anno.value();

			for (String entityAttributeName : entityAttributeNames) {
				accessors.add(new AccessorPair(entityType, entityAttributeName, modelType, modelAttributeName));
			}
		}

		this.accessors = Collections.unmodifiableList(accessors);
	}

	@Override
	public <E extends T, N extends M> E extract(N source, E target) {
		accessors.stream().forEach(accessor -> accessor.execute(source, target));
		return target;
	}

	private class AccessorPair {

		private final Getter modelAttributeGetter;
		private final Setter destinationAttributeSetter;
		private final List<PropertyAccess> entityNodeAccessors;
		private final List<Instantiator> entityNodeInstantiators;

		public AccessorPair(Class<T> entityType, String entityAttributeName, Class<M> modelType,
				String modelAttributeName) {
			super();
			this.modelAttributeGetter = StandardAccess.locateGetter(modelType, modelAttributeName)
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("Unable to locate getter for attribute [%s] in model type [%s]",
									modelAttributeName, modelType.getName())));

			List<PropertyAccess> entityNodeAccessors = new ArrayList<>(0);
			List<Instantiator> entityNodeInstantiators = new ArrayList<>(0);
			String[] entityAttributeNodes = entityAttributeName.split("\\.");
			int n = entityAttributeNodes.length;
			Class<?> nodeType = entityType, parentType = nodeType;

			for (int i = 0; i < n - 1; i++, parentType = nodeType) {
				String node = entityAttributeNodes[i];

				try {
					nodeType = nodeType.getDeclaredField(node).getType();
				} catch (NoSuchFieldException | SecurityException e) {
					throw new IllegalArgumentException(e);
				}

				entityNodeAccessors.add(
						PropertyAccessStrategyFactory.STANDARD_ACCESS_STRATEGY.buildPropertyAccess(parentType, node));
				entityNodeInstantiators.add(new PojoInstantiator(nodeType, null));
			}

			this.destinationAttributeSetter = StandardAccess.locateSetter(nodeType, entityAttributeNodes[n - 1])
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("Unable to locate setter for attribute [%s] in model type [%s]",
									entityAttributeName, entityType.getName())));
			this.entityNodeAccessors = Collections.unmodifiableList(entityNodeAccessors);
			this.entityNodeInstantiators = Collections.unmodifiableList(entityNodeInstantiators);
		}

		public void execute(M model, T entity) {
			final Object value = modelAttributeGetter.get(model);
			final Wrapper<Object> destination = new Wrapper<>(entity);

			IntStream.range(0, entityNodeAccessors.size()).forEach(index -> {
				PropertyAccess accessor = entityNodeAccessors.get(index);
				Object val = Optional.ofNullable(accessor.getGetter().get(destination.getValue()))
						.orElse(entityNodeInstantiators.get(index).instantiate());

				accessor.getSetter().set(destination.getValue(), val, null);
				destination.setValue(val);
			});

			destinationAttributeSetter.set(destination.getValue(), value, null);
		}

	}

}
