/**
 * 
 */
package adn.model.entities.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.metamodel.Attribute;

import org.hibernate.MappingException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.IdentifierProperty;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.slf4j.Logger;
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
	private final Set<String> nonLazyProperties;
	private final int nonLazyPropertiesSpan;
	private final Set<Map.Entry<String, Getter>> getters;

	@SuppressWarnings("unchecked")
	public <T extends AbstractModel> EntityMetadataImpl(final ModelContextProvider modelContext, Class<T> entityClass) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		Set<Map.Entry<String, Getter>> getters;
		Set<String> properties;
		Set<String> nonLazyProperties;

		try {
			if (!Entity.class.isAssignableFrom(entityClass)) {
				throw new MappingException("Not a HBM entity");
			}

			Class<? extends Entity> type = (Class<? extends Entity>) entityClass;
			EntityPersister persister = EntityUtils.getEntityPersister(type);
			EntityMetamodel metamodel = persister.getEntityMetamodel();
			EntityTuplizer tuplizer = metamodel.getTuplizer();

			getters = Stream.of(metamodel.getPropertyNames())
					.map(name -> Map.entry(name, tuplizer.getGetter(metamodel.getPropertyIndex(name))))
					.collect(Collectors.toSet());
			properties = Stream.of(metamodel.getPropertyNames()).collect(Collectors.toSet());
			// notice-start: to following before adding identifiers
			boolean[] laziness = metamodel.getPropertyLaziness();
			Map<String, Attribute<T, ?>> attrs = persister.getFactory().getMetamodel().entity(type).getAttributes()
					.stream().map(attr -> Map.entry(attr.getName(), (Attribute<T, ?>) attr))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			nonLazyProperties = properties.stream().filter(prop -> {
				Attribute<T, ?> attr = attrs.get(prop);

				if (attr == null) {
					logger.warn(String.format("Null attribute [%s] in [%s], potentially a virtual property", prop,
							type.getName()));
					return false;
				}

				if (!Collection.class.isAssignableFrom(attr.getJavaType())) {
					return !laziness[metamodel.getPropertyIndex(prop)];
				}

				if (!(attr.getJavaMember() instanceof Field)) {
					return true;
				}

				ParameterizedType genericTypes = (ParameterizedType) ((Field) attr.getJavaMember()).getGenericType();
				Class<?> clz = (Class<?>) genericTypes.getActualTypeArguments()[0];

				if (!Entity.class.isAssignableFrom(clz)) {
					return true;
				}

				// this is how a CollectionPersister role is resolved by Hibernate
				// see org.hibernate.cfg.annotations.CollectionBinder.bind()
				String collectionRole = StringHelper.qualify(type.getName(), prop);

				try {
					return !persister.getFactory().getMetamodel().collectionPersister(collectionRole).isLazy();
				} catch (MappingException me) {
					return true;
				}
			}).collect(Collectors.toSet());
			// notice-end:
			IdentifierProperty identifier = metamodel.getIdentifierProperty();

			if (!identifier.isVirtual()) {
				getters.add(Map.entry(metamodel.getIdentifierProperty().getName(), tuplizer.getIdentifierGetter()));
				properties.add(metamodel.getIdentifierProperty().getName());
				nonLazyProperties.add(metamodel.getIdentifierProperty().getName());
			}
		} catch (MappingException me) {
			if (logger.isTraceEnabled()) {
				me.printStackTrace();
				logger.error(
						String.format("Unable to resolve getters of [%s] from Hibernate, resolving getters through %s",
								entityClass.getName(), StandardAccess.class.getName()));
			}

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
										String.format("Unable to locate getter for property [%s] in type [%s]", name,
												entityClass.getName()))));
			}

			getters = accessors.entrySet();
			nonLazyProperties = properties;
		}

		this.getters = getters;
		this.properties = properties;
		this.nonLazyProperties = nonLazyProperties;
		nonLazyPropertiesSpan = this.nonLazyProperties.size();
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
	public Set<String> getNonLazyPropertyNames() {
		return nonLazyProperties;
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
				+ "\t],\n"
				+ "\tnonLazyProperties=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ ")",
				this.getClass().getSimpleName(),
				properties.stream().collect(Collectors.joining(", ")),
				nonLazyProperties.stream().collect(Collectors.joining(", ")));
		// @formatter:on
	}

	@Override
	public int getNonLazyPropertiesSpan() {
		return nonLazyPropertiesSpan;
	}

}
