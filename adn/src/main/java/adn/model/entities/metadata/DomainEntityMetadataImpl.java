/**
 * 
 */
package adn.model.entities.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import org.hibernate.MappingException;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.IdentifierProperty;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.ComponentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.builders.ModelContextProvider;
import adn.engine.access.StandardAccess;
import adn.helpers.HibernateHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainEntity;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public class DomainEntityMetadataImpl<T extends DomainEntity> implements DomainEntityMetadata<T> {

	private final Class<T> entityType;

	private final Set<String> properties;
	private final Set<String> declaredProperties;
	private final Set<String> nonLazyProperties;
	private final int nonLazyPropertiesSpan;
	private final int propertiesSpan;
	private final Set<Map.Entry<String, Getter>> getters;
	private final String discriminatorColumnName;
	private final Map<String, Class<?>> propertyTypes;
	private final Map<String, Class<? extends DomainEntity>> associationTypes;

	@SuppressWarnings("unchecked")
	public DomainEntityMetadataImpl(final ModelContextProvider modelContext, Class<T> entityClass) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		Set<Map.Entry<String, Getter>> getters;
		Set<String> properties;
		Set<String> nonLazyProperties;
		String discriminatorColumnName = null;
		Set<String> declaredProperties;
		Map<String, Class<?>> propertyTypes;

		try {
			if (!Entity.class.isAssignableFrom(entityClass)) {
				throw new MappingException("Not a HBM entity");
			}

			Class<? extends Entity> type = (Class<? extends Entity>) entityClass;
			EntityPersister persister = HibernateHelper.getEntityPersister(type);
			EntityMetamodel metamodel = persister.getEntityMetamodel();
			EntityTuplizer tuplizer = metamodel.getTuplizer();

			getters = Stream.of(metamodel.getPropertyNames())
					.map(name -> Map.entry(name, tuplizer.getGetter(metamodel.getPropertyIndex(name))))
					.collect(Collectors.toSet());

			properties = Stream.of(metamodel.getPropertyNames()).collect(Collectors.toSet());
			// notice-start: do following before adding identifier
			boolean[] laziness = metamodel.getPropertyLaziness();
			EntityType<? extends Entity> persistenceType = persister.getFactory().getMetamodel().entity(type);
			Map<String, Attribute<T, ?>> attrs = persistenceType.getAttributes().stream()
					.map(attr -> Map.entry(attr.getName(), (Attribute<T, ?>) attr))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			propertyTypes = persistenceType.getAttributes().stream()
					.map(attr -> Map.entry(attr.getName(), (Class<? extends DomainEntity>) attr.getJavaType()))
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
				String identifierName = metamodel.getIdentifierProperty().getName();

				getters.add(Map.entry(identifierName, tuplizer.getIdentifierGetter()));
				properties.add(identifierName);
				nonLazyProperties.add(identifierName);
				propertyTypes.put(identifierName, identifier.getType().getReturnedClass());

				if (identifier.getType() instanceof ComponentType) {
					ComponentType component = (ComponentType) identifier.getType();
					List<String> componentProperties = Arrays.asList(component.getPropertyNames());

					for (String componentProperty : componentProperties) {
						properties.add(componentProperty);
						nonLazyProperties.add(componentProperty);
						propertyTypes.put(componentProperty,
								component.getSubtypes()[component.getPropertyIndex(componentProperty)]
										.getReturnedClass());
					}
				}
			}

			if (persister instanceof SingleTableEntityPersister) {
				SingleTableEntityPersister singleTableEntityPersister = (SingleTableEntityPersister) persister;

				discriminatorColumnName = singleTableEntityPersister.getDiscriminatorColumnName();

				if (discriminatorColumnName != null) {
					properties.add(discriminatorColumnName);
					propertyTypes.put(discriminatorColumnName, singleTableEntityPersister.getType().getReturnedClass());
				}
			}

			declaredProperties = persistenceType.getDeclaredAttributes().stream().map(attr -> attr.getName())
					.collect(Collectors.toSet());
		} catch (MappingException me) {
			if (logger.isTraceEnabled()) {
				me.printStackTrace();
				logger.error(
						String.format("Unable to resolve getters of [%s] from Hibernate, resolving getters through %s",
								entityClass.getName(), StandardAccess.class.getName()));
			}

			Class<?> superClass = entityClass.getSuperclass();
			DomainEntityMetadataImpl<?> superMetadata = null;

			while (superClass != null && superClass != Object.class
					&& ((superMetadata) = (DomainEntityMetadataImpl<?>) modelContext
							.getMetadata((Class<DomainEntity>) superClass)) == null) {
				superClass = superClass.getSuperclass();
			}

			properties = new HashSet<>(0, 1f);
			propertyTypes = new HashMap<>(0, 1f);

			if (superMetadata != null) {
				properties.addAll(superMetadata.getPropertyNames());
				propertyTypes.putAll(superMetadata.getPropertyTypes());
			}

			declaredProperties = new HashSet<>();

			for (Field f : entityClass.getDeclaredFields()) {
				if (Modifier.isTransient(f.getModifiers())) {
					continue;
				}

				properties.add(f.getName());
				propertyTypes.put(f.getName(), (Class<? extends DomainEntity>) f.getType());
				declaredProperties.add(f.getName());
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

		this.entityType = entityClass;
		this.getters = Collections.unmodifiableSet(getters);
		this.properties = Collections.unmodifiableSet(properties);
		this.propertyTypes = Collections.unmodifiableMap(propertyTypes);
		this.declaredProperties = Collections.unmodifiableSet(declaredProperties);
		this.nonLazyProperties = Collections.unmodifiableSet(nonLazyProperties);
		nonLazyPropertiesSpan = this.nonLazyProperties.size();
		propertiesSpan = this.properties.size();
		this.discriminatorColumnName = discriminatorColumnName;
		this.associationTypes = Collections.unmodifiableMap(propertyTypes.entrySet().stream().filter(entry -> {
			if (DomainEntity.class.isAssignableFrom(entry.getValue())) {
				return true;
			}

			if (!Collection.class.isAssignableFrom(entry.getValue())) {
				return false;
			}

			try {
				return DomainEntity.class.isAssignableFrom(
						((Class<?>) TypeHelper.getGenericType(entityClass.getDeclaredField(entry.getKey()))));
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
				return false;
			}
		}).map(filteredEntry -> Map.entry(filteredEntry.getKey(),
				(Class<? extends DomainEntity>) filteredEntry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	private Map<String, Class<?>> getPropertyTypes() {
		return propertyTypes;
	}

	@Override
	public Class<T> getType() {
		return entityType;
	}

	@Override
	public boolean hasProperty(String attributeName) {
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
		if (properties.isEmpty()) {
			return String.format("This %s is empty", this.getClass().getSimpleName());
		}
		// @formatter:off
		return String.format("%s(\n"
				+ "\tproperties=[\n"
				+ "\t\t%s\n"
				+ "\t],\n"
				+ "\tnonLazyProperties=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ "\tassociationTypes=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ ")",
				this.getClass().getSimpleName(),
				properties.stream().collect(Collectors.joining(", ")),
				nonLazyProperties.stream().collect(Collectors.joining(", ")),
				associationTypes.entrySet().stream()
					.map(entry -> String.format("%s|%s", entry.getKey(), entry.getValue().getSimpleName())).collect(Collectors.joining(", ")));
		// @formatter:on
	}

	@Override
	public int getNonLazyPropertiesSpan() {
		return nonLazyPropertiesSpan;
	}

	@Override
	public int getPropertiesSpan() {
		return propertiesSpan;
	}

	@Override
	public String getDiscriminatorColumnName() {
		return discriminatorColumnName;
	}

	@Override
	public Set<String> getDeclaredPropertyNames() {
		return declaredProperties;
	}

	@Override
	public boolean isEntityType(String attributeName) {
		return DomainEntity.class.isAssignableFrom(propertyTypes.get(attributeName));
	}

	@Override
	public boolean isAssociation(String attributeName) {
		return associationTypes.containsKey(attributeName);
	}

	@Override
	public Class<?> getPropertyType(String propertyName) {
		return propertyTypes.get(propertyName);
	}

	@Override
	public Class<? extends DomainEntity> getAssociationType(String associationName) {
		return associationTypes.get(associationName);
	}

}
