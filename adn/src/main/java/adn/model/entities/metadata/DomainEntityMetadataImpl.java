/**
 * 
 */
package adn.model.entities.metadata;

import static java.util.Map.entry;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
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
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.IdentifierProperty;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.CollectionType;
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

	private static final Set<String> SENSITIVE_FIELD_NAMES;

	static {
		Set<String> sensitiveFields = new HashSet<>();

		sensitiveFields.add(_Account.password);

		SENSITIVE_FIELD_NAMES = Collections.unmodifiableSet(sensitiveFields);
	}

	private final Class<T> entityType;

	private final List<String> properties;
	private final List<String> declaredProperties;
	private final List<String> nonLazyProperties;
	private final int propertiesSpan;
	private final List<Map.Entry<String, Getter>> getters;
	private final String discriminatorColumnName;
	private final Map<String, Class<?>> propertyTypes;
	private final Map<String, Class<? extends DomainEntity>> associationsClass;
	private final Map<String, AssociationType> associationTypes;
	private final Set<String> optionalAssociations;

	@SuppressWarnings("unchecked")
	public DomainEntityMetadataImpl(final ModelContextProvider modelContext, Class<T> entityClass) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		List<Map.Entry<String, Getter>> getters;
		List<String> properties;
		List<String> nonLazyProperties;
		String discriminatorColumnName = null;
		List<String> declaredProperties;
		Map<String, Class<?>> propertyTypes;
		Set<String> optionalAssociations = new HashSet<>();
		Map<String, AssociationType> associationTypes = new HashMap<>();

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
					.collect(Collectors.toList());

			properties = Stream.of(metamodel.getPropertyNames()).collect(Collectors.toList());
			// notice-start: do following before adding identifier
			boolean[] laziness = metamodel.getPropertyLaziness();
			EntityType<? extends Entity> persistenceType = persister.getFactory().getMetamodel().entity(type);
			Map<String, Attribute<T, ?>> attrs = persistenceType.getAttributes().stream()
					.map(attr -> Map.entry(attr.getName(), (Attribute<T, ?>) attr))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			propertyTypes = persistenceType.getAttributes().stream()
					.map(attr -> Map.entry(attr.getName(), (Class<? extends DomainEntity>) attr.getJavaType()))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			associationTypes = properties.stream()
					.map(prop -> entry(prop, metamodel.getPropertyTypes()[metamodel.getPropertyIndex(prop)]))
					.filter(entry -> org.hibernate.type.AssociationType.class
							.isAssignableFrom(entry.getValue().getClass()))
					.map(entry -> CollectionType.class.isAssignableFrom(entry.getValue().getClass())
							? entry(entry.getKey(), AssociationType.COLLECTION)
							: org.hibernate.type.EntityType.class.isAssignableFrom(entry.getValue().getClass())
									? entry(entry.getKey(), AssociationType.ENTITY)
									: entry(entry.getKey(), AssociationType.ANY))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

			nonLazyProperties = properties.stream().filter(prop -> {
				Attribute<T, ?> attr = attrs.get(prop);

				if (attr == null) {
					logger.warn(String.format("Null attribute [%s] in [%s], potentially a virtual property", prop,
							type.getName()));
					return false;
				}

				if (!Collection.class.isAssignableFrom(attr.getJavaType())) {
					int propertyIndex = metamodel.getPropertyIndex(prop);

					if (Entity.class.isAssignableFrom(attr.getJavaType())) {
						org.hibernate.type.EntityType associationType = (org.hibernate.type.EntityType) metamodel
								.getPropertyTypes()[propertyIndex];

						if (metamodel.getPropertyNullability()[propertyIndex]) {
							optionalAssociations.add(prop);
						}

						return associationType.isEager(null);
					}

					return !laziness[propertyIndex];
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
			}).collect(Collectors.toList());
			// notice-end:
			IdentifierProperty identifier = metamodel.getIdentifierProperty();

			if (!identifier.isVirtual()) {
				String identifierName = identifier.getName();
				Getter identifierGetter = tuplizer.getIdentifierGetter();
				Class<?> identifierJavaClass = identifier.getType().getReturnedClass();

				getters.add(entry(identifierName, identifierGetter));
				properties.add(identifierName);
				nonLazyProperties.add(identifierName);
				propertyTypes.put(identifierName, identifierJavaClass);

				if (identifier.getType() instanceof ComponentType) {
					ComponentType component = (ComponentType) identifier.getType();
					List<String> componentPropertiesNames = Arrays.asList(component.getPropertyNames());
					Class<?> componentPropertyType;

					for (String componentPropertyName : componentPropertiesNames) {
						properties.add(componentPropertyName);
						nonLazyProperties.add(componentPropertyName);
						componentPropertyType = component.getSubtypes()[component
								.getPropertyIndex(componentPropertyName)].getReturnedClass();
						propertyTypes.put(componentPropertyName, componentPropertyType);
						getters.add(entry(componentPropertyName, new ComponentPropertyGetter(identifierGetter,
								identifierJavaClass, componentPropertyName, componentPropertyType)));
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
					.collect(Collectors.toList());
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

			properties = new ArrayList<>(0);
			propertyTypes = new HashMap<>(0, 1f);
			Map<String, Getter> accessors = new HashMap<>(0, 1f);

			if (superMetadata != null) {
				properties.addAll(superMetadata.getPropertyNames());
				propertyTypes.putAll(superMetadata.getPropertyTypes());
				accessors.putAll(superMetadata.getGetters().stream()
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			}

			declaredProperties = new ArrayList<>(0);

			for (Field f : entityClass.getDeclaredFields()) {
				if (Modifier.isTransient(f.getModifiers())) {
					continue;
				}

				properties.add(f.getName());
				propertyTypes.put(f.getName(), (Class<? extends DomainEntity>) f.getType());
				declaredProperties.add(f.getName());
			}

			for (String name : declaredProperties) {
				accessors.put(name,
						StandardAccess.locateGetter(entityClass, name)
								.orElseThrow(() -> new IllegalArgumentException(
										String.format("Unable to locate getter for property [%s] in type [%s]", name,
												entityClass.getName()))));
			}

			getters = new ArrayList<>(accessors.entrySet());
			nonLazyProperties = properties;
		}

		this.entityType = entityClass;
		this.getters = Collections.unmodifiableList(getters);
		this.properties = Collections.unmodifiableList(properties);
		this.propertyTypes = Collections.unmodifiableMap(propertyTypes);
		this.declaredProperties = Collections.unmodifiableList(declaredProperties);
		this.nonLazyProperties = Collections.unmodifiableList(nonLazyProperties.stream()
				.filter(propName -> !SENSITIVE_FIELD_NAMES.contains(propName)).collect(Collectors.toList()));
		this.optionalAssociations = Collections.unmodifiableSet(optionalAssociations);
		propertiesSpan = this.properties.size();
		this.discriminatorColumnName = discriminatorColumnName;
		// @formatter:off
		Map<String, Class<? extends DomainEntity>> associationsClass = new HashMap<>(0);
		
		propertyTypes.entrySet().stream().forEach(entry -> {
			Class<?> type = entry.getValue();
			
			if (DomainEntity.class.isAssignableFrom(type)) {
				associationsClass.put(entry.getKey(), (Class<? extends DomainEntity>) type);
				return;
			}
			
			if (Collection.class.isAssignableFrom(type)) {
				try {
					Class<?> genericType = (Class<?>) TypeHelper.getGenericType(entityClass.getDeclaredField(entry.getKey()));
					
					if (DomainEntity.class.isAssignableFrom(genericType)) {
						associationsClass.put(entry.getKey(), (Class<? extends DomainEntity>) genericType);
					}
				} catch (NoSuchFieldException | SecurityException e) {
					return;
				}
			}
		});

		this.associationsClass = Collections.unmodifiableMap(associationsClass);
		this.associationTypes = Collections.unmodifiableMap(associationTypes);
		// @formatter:on
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
	public List<String> getPropertyNames() {
		return properties;
	}

	@Override
	public List<String> getNonLazyPropertyNames() {
		return nonLazyProperties;
	}

	@Override
	public List<Entry<String, Getter>> getGetters() {
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
				+ "\tassociationsClass=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ "\tassociationTypes=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ ")",
				this.getClass().getSimpleName(),
				properties.stream().collect(Collectors.joining(", ")),
				nonLazyProperties.stream().collect(Collectors.joining(", ")),
				associationsClass.entrySet().stream()
					.map(entry -> String.format("%s|%s", entry.getKey(), entry.getValue().getSimpleName())).collect(Collectors.joining(", ")),
				associationTypes.entrySet().stream()
					.map(entry -> String.format("%s|%s", entry.getKey(), entry.getValue())).collect(Collectors.joining(", ")));
		// @formatter:on
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
	public List<String> getDeclaredPropertyNames() {
		return declaredProperties;
	}

	@Override
	public boolean isAssociation(String attributeName) {
		return associationsClass.containsKey(attributeName);
	}

	@Override
	public Class<?> getPropertyType(String propertyName) {
		return propertyTypes.get(propertyName);
	}

	@Override
	public Class<? extends DomainEntity> getAssociationClass(String associationName) {
		return associationsClass.get(associationName);
	}

	@Override
	public boolean isAssociationOptional(String attributeName) {
		return optionalAssociations.contains(attributeName);
	}

	@Override
	public AssociationType getAssociationType(String associationName) {
		return associationTypes.get(associationName);
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	private class ComponentPropertyGetter implements Getter {

		private final Getter componentGetter;
		private final Getter propertyGetter;

		private ComponentPropertyGetter(Getter componentGetter, Class<?> componentType, String propertyName,
				Class<?> propertyType) {
			this.componentGetter = componentGetter;
			propertyGetter = StandardAccess.locateGetter(componentType, propertyName)
					.orElseThrow(() -> new IllegalArgumentException(String.format(
							"Unable to locate getter for a Component's property. Component type: [%s], property type: [%s]",
							componentType.getName(), propertyName)));
		}

		@Override
		public Object get(Object owner) {
			Object componentValue = componentGetter.get(owner);

			return propertyGetter.get(componentValue);
		}

		@Override
		public Object getForInsert(Object owner, Map mergeMap, SharedSessionContractImplementor session) {
			return get(owner);
		}

		@Override
		public Class getReturnType() {
			return propertyGetter.getReturnType();
		}

		@Override
		public Member getMember() {
			return propertyGetter.getMember();
		}

		@Override
		public String getMethodName() {
			return propertyGetter.getMethodName();
		}

		@Override
		public Method getMethod() {
			return propertyGetter.getMethod();
		}

	}

}
