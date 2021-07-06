/**
 * 
 */
package adn.model.entities.metadata;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.MappingException;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.slf4j.LoggerFactory;

import adn.engine.access.StandardAccess;
import adn.helpers.EntityUtils;
import adn.model.AbstractModel;
import adn.model.ModelContextProvider;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class EntityMetadataImpl implements EntityMetadata {

	private final Set<String> properties;
	private final Set<Map.Entry<String, Getter>> getters;

	@SuppressWarnings("unchecked")
	public <T extends AbstractModel> EntityMetadataImpl(final ModelContextProvider modelContext, Class<T> entityClass) {
		Set<Map.Entry<String, Getter>> getters;
		Set<String> properties;

		try {
			if (!Entity.class.isAssignableFrom(entityClass)) {
				throw new MappingException("Not a HBM entity");
			}

			Class<? extends Entity> type = (Class<? extends Entity>) entityClass;
			EntityMetamodel metamodel = EntityUtils.getEntityPersister(type).getEntityMetamodel();
			EntityTuplizer tuplizer = metamodel.getTuplizer();

			getters = Stream.of(metamodel.getPropertyNames())
					.map(name -> Map.entry(name, tuplizer.getGetter(metamodel.getPropertyIndex(name))))
					.collect(Collectors.toSet());
			properties = Stream.of(metamodel.getPropertyNames()).collect(Collectors.toSet());

			getters.add(Map.entry(metamodel.getIdentifierProperty().getName(), tuplizer.getIdentifierGetter()));
			properties.add(metamodel.getIdentifierProperty().getName());
		} catch (MappingException e) {
			LoggerFactory.getLogger(this.getClass())
					.error(String.format(
							"Unable to resolve getters of [%s] from Hibernate, resolving getters through %s",
							entityClass.getName(), StandardAccess.class.getName()));
			Class<?> superClass = entityClass.getSuperclass();
			EntityMetadata superMetadata = null;

			while (superClass != null && superClass != Object.class
					&& ((superMetadata) = modelContext.getMetadata((Class<AbstractModel>) superClass)) == null) {
				superClass = superClass.getSuperclass();
			}

			properties = new HashSet<>(0, 1f);

			if (superMetadata != null) {
				properties.addAll(superMetadata.getPropertyNames());
			}

			for (Field f : entityClass.getDeclaredFields()) {
				properties.add(f.getName());
			}

			int propertySpan = properties.size();
			Map<String, Getter> accessors = new HashMap<>(propertySpan, 1.1f);

			for (String name : properties) {
				accessors.put(name,
						StandardAccess.locateGetter(entityClass, name)
								.orElseThrow(() -> new IllegalArgumentException(
										String.format("Unable to locate getter for property [%s] in type [%]", name,
												entityClass.getName()))));
			}

			getters = accessors.entrySet();
		}

		this.getters = getters;
		this.properties = properties;
	}

	@Override
	public boolean hasAttribute(String attributeName) {
		return properties.contains(attributeName);
	}

	@Override
	public Set<String> getPropertyNames() {
		return properties;
	}

	@Override
	public Set<Entry<String, Getter>> getGetters() {
		return getters;
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("%s(\n"
				+ "\tproperties=[\n"
				+ "\t\t%s\n"
				+ "\t]",
				this.getClass().getSimpleName(),
				properties.stream().collect(Collectors.joining(", ")));
		// @formatter:on
	}

}
