/**
 * 
 */
package adn.model.entities.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.IdentifierProperty;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.builders.ModelContextProvider;
import adn.engine.access.StandardAccess;
import adn.helpers.HibernateHelper;
import adn.helpers.TypeHelper;
import adn.model.DomainComponentType;
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

		sensitiveFields.add(_User.password);

		SENSITIVE_FIELD_NAMES = Collections.unmodifiableSet(sensitiveFields);
	}

	private final Class<T> entityType;

	private final List<String> properties;
	private final List<String> nonLazyProperties;
	private final Map<String, Class<?>> propertyTypes;
	private final Map<String, Getter> getters;
	private final Map<String, Class<? extends DomainEntity>> associationClassMap;
	private final Map<String, AssociationType> associationTypeMap;
	private final Set<String> optionalAssociations;

	@SuppressWarnings("unchecked")
	public DomainEntityMetadataImpl(final ModelContextProvider modelContext, Class<T> entityType)
			throws NoSuchFieldException, SecurityException {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		List<String> properties;
		Map<String, Class<?>> propertyTypes;
		List<String> nonLazyProperties;
		Map<String, Getter> getters;
		Map<String, Class<? extends DomainEntity>> associationClassMap;
		Map<String, AssociationType> associationTypeMap;
		Set<String> optionalAssociations;

		this.entityType = entityType;

		try {
			if (!Entity.class.isAssignableFrom(entityType)) {
				throw new MappingException("Not a HBM entity");
			}

			Class<? extends Entity> type = (Class<? extends Entity>) entityType;
			EntityPersister persister = HibernateHelper.getEntityPersister(type);
			EntityMetamodel metamodel = persister.getEntityMetamodel();
			EntityTuplizer tuplizer = persister.getEntityTuplizer();
			boolean[] propertyLaziness = metamodel.getPropertyLaziness();
			boolean[] propertyNullabilities = metamodel.getPropertyNullability();

			properties = new ArrayList<>(0);
			propertyTypes = new HashMap<>(0, 1f);
			nonLazyProperties = new ArrayList<>(0);
			getters = new HashMap<>(0, 1f);
			associationClassMap = new HashMap<>(0, 1f);
			associationTypeMap = new HashMap<>(0, 1f);
			optionalAssociations = new HashSet<>(0);

			for (String property : metamodel.getPropertyNames()) {
				int propertyIndex = metamodel.getPropertyIndex(property);
				Type propertyType = metamodel.getPropertyTypes()[propertyIndex];
				Getter propertyGetter = tuplizer.getGetter(propertyIndex);
				Class<?> propertyClass = propertyType.getReturnedClass();

				getters.put(property, propertyGetter);

				if (!ComponentType.class.isAssignableFrom(propertyType.getClass())) {
					properties.add(property);
					propertyTypes.put(property, propertyClass);
					// @formatter:off
					resolvePropertyLaziness(
							property,
							propertyType,
							propertyIndex,
							propertyLaziness,
							nonLazyProperties,
							entityType);
					
					if (org.hibernate.type.AssociationType.class.isAssignableFrom(propertyType.getClass())) {
						resolveAssociationMetadata(
								entityType,
								property,
								propertyType,
								propertyIndex,
								propertyNullabilities,
								associationClassMap,
								associationTypeMap,
								optionalAssociations);
					}
					// @formatter:on
					continue;
				}

				ComponentType componentType = (ComponentType) propertyType;
				Type componentPropertyType;
				int componentPropertyIndex;
				Class<?> componentPropertyClass;
				boolean[] componentPropertyLaziness = new boolean[componentType.getPropertyNames().length];
				boolean[] componentPropertyNullabilities = componentType.getPropertyNullability();
				// assumes all properties in a component is non-lazy
				Arrays.fill(componentPropertyLaziness, false);

				for (String componentProperty : componentType.getPropertyNames()) {
					componentPropertyIndex = componentType.getPropertyIndex(componentProperty);
					properties.add(componentProperty);
					componentPropertyType = componentType.getSubtypes()[componentPropertyIndex];
					componentPropertyClass = componentPropertyType.getReturnedClass();
					propertyTypes.put(componentProperty, componentPropertyClass);
					// @formatter:off
					resolvePropertyLaziness(
							componentProperty,
							componentPropertyType,
							componentPropertyIndex,
							componentPropertyLaziness,
							nonLazyProperties,
							componentPropertyClass);
					getters.put(componentProperty, new ComponentPropertyGetter(
							propertyGetter,
							propertyClass,
							componentProperty,
							componentPropertyClass));

					if (org.hibernate.type.AssociationType.class.isAssignableFrom(componentPropertyType.getClass())) {
						resolveAssociationMetadata(
								componentPropertyClass,
								componentProperty,
								componentPropertyType,
								componentPropertyIndex,
								componentPropertyNullabilities,
								associationClassMap,
								associationTypeMap,
								optionalAssociations);
					}
					// @formatter:on
				}
			}

			IdentifierProperty identifier = metamodel.getIdentifierProperty();

			if (!identifier.isVirtual()) {
				String identifierName = identifier.getName();
				Getter identifierGetter = tuplizer.getIdentifierGetter();
				Class<?> identifierJavaClass = identifier.getType().getReturnedClass();

				getters.put(identifierName, identifierGetter);
				properties.add(identifierName);
				nonLazyProperties.add(identifierName);
				propertyTypes.put(identifierName, identifierJavaClass);

				if (identifier.getType() instanceof ComponentType) {
					ComponentType component = (ComponentType) identifier.getType();
					Class<?> identifierPropertyClass;
					int identifierPropertyIndex;
					Type identifierPropertyType;
					boolean[] identifierPropertyNullabilities = component.getPropertyNullability();

					for (String identifierPropertyName : component.getPropertyNames()) {
						identifierPropertyIndex = component.getPropertyIndex(identifierPropertyName);
						identifierPropertyType = component.getSubtypes()[identifierPropertyIndex];
						properties.add(identifierPropertyName);
						nonLazyProperties.add(identifierPropertyName);
						identifierPropertyClass = identifierPropertyType.getReturnedClass();
						propertyTypes.put(identifierPropertyName, identifierPropertyClass);
						getters.put(identifierPropertyName, new ComponentPropertyGetter(identifierGetter,
								identifierJavaClass, identifierPropertyName, identifierPropertyClass));
						// @formatter:off
						if (org.hibernate.type.AssociationType.class.isAssignableFrom(identifierPropertyType.getClass())) {
							resolveAssociationMetadata(
									identifierPropertyClass,
									identifierPropertyName,
									identifierPropertyType,
									identifierPropertyIndex,
									identifierPropertyNullabilities,
									associationClassMap,
									associationTypeMap,
									optionalAssociations);
						}
						// @formatter:on
					}
				}
			}
		} catch (MappingException me) {
			// this block resolves either Hibernate's abstract entities or
			// entities of type adn.model.models.Model
			// TODO: make this class into two classes, one for Hibernate's entities, the
			// other is for non-Hibernate's entities
			if (logger.isTraceEnabled()) {
				logger.debug(String.format("Unable to resolve [%s] from Hibernate", entityType.getName()));
			}

			Class<?> superClass = entityType.getSuperclass();
			DomainEntityMetadataImpl<? extends DomainEntity> superMetadata = null;

			while (superClass != Object.class && (superMetadata = (DomainEntityMetadataImpl<?>) modelContext
					.getMetadata((Class<DomainEntity>) superClass)) == null) {
				superClass = superClass.getSuperclass();
			}

			properties = new ArrayList<>(0);
			propertyTypes = new HashMap<>(0, 1f);
			nonLazyProperties = new ArrayList<>(0);
			getters = new HashMap<>(0, 1f);
			associationClassMap = new HashMap<>(0, 1f);
			associationTypeMap = new HashMap<>(0, 1f);
			optionalAssociations = new HashSet<>(0);

			if (superMetadata != null) {
				properties.addAll(superMetadata.properties);
				propertyTypes.putAll(superMetadata.propertyTypes);
				nonLazyProperties.addAll(superMetadata.nonLazyProperties);
				getters.putAll(superMetadata.getters);
				associationClassMap.putAll(superMetadata.associationClassMap);
				associationTypeMap.putAll(superMetadata.associationTypeMap);
				optionalAssociations.addAll(superMetadata.optionalAssociations);
			}

			for (Field field : entityType.getDeclaredFields()) {
				String propertyName = field.getName();
				Class<?> propertyType = field.getType();
				Getter propertyGetter = StandardAccess.locateGetter(entityType, propertyName)
						.orElseThrow(() -> new IllegalArgumentException(
								String.format("Unable to locate getter for property [%s] in type [%s]", propertyName,
										entityType.getName())));

				getters.put(propertyName, propertyGetter);

				if (!DomainComponentType.class.isAssignableFrom(propertyType)) {
					// @formatter:off
					resolveNonHibernateProperty(
							entityType,
							field,
							properties,
							propertyTypes,
							nonLazyProperties,
							associationClassMap,
							associationTypeMap,
							optionalAssociations);
					// @formatter:on
					continue;
				}

				for (Field componentField : propertyType.getDeclaredFields()) {
					String propertyComponentName = componentField.getName();
					// @formatter:off
					resolveNonHibernateProperty(
							propertyType,
							componentField,
							properties,
							propertyTypes,
							nonLazyProperties,
							associationClassMap,
							associationTypeMap,
							optionalAssociations);
					getters.put(propertyComponentName, new ComponentPropertyGetter(
							propertyGetter,
							propertyType,
							propertyComponentName,
							componentField.getType()));
					// @formatter:on
				}
			}
		}

		this.properties = Collections.unmodifiableList(properties);
		this.propertyTypes = Collections.unmodifiableMap(propertyTypes);
		this.nonLazyProperties = Collections.unmodifiableList(nonLazyProperties.stream()
				.filter(property -> !SENSITIVE_FIELD_NAMES.contains(property)).collect(Collectors.toList()));
		this.getters = Collections.unmodifiableMap(getters);
		this.associationClassMap = Collections.unmodifiableMap(associationClassMap);
		this.associationTypeMap = Collections.unmodifiableMap(associationTypeMap);
		this.optionalAssociations = Collections.unmodifiableSet(optionalAssociations);
	}

	// @formatter:off
	@SuppressWarnings("unchecked")
	private void resolveNonHibernateProperty(
			Class<?> owningType,
			Field propertyField,
			List<String > properties,
			Map<String, Class<?>> propertyTypes,
			List<String > nonLazyProperties,
			Map<String, Class<? extends DomainEntity>> associationClassMap,
			Map<String, AssociationType> associationTypeMap,
			Set<String> optionalAssociations
			) {
		String propertyName = propertyField.getName();
		Class<?> propertyType = propertyField.getType();

		properties.add(propertyName);
		propertyTypes.put(propertyName, propertyType);
		
		Basic basicAnno = propertyField.getDeclaredAnnotation(Basic.class);

		if (basicAnno == null || basicAnno.fetch() == FetchType.EAGER) {
			nonLazyProperties.add(propertyName);
		}

		if (Entity.class.isAssignableFrom(propertyType)) {
			associationClassMap.put(propertyName,
					(Class<? extends DomainEntity>) propertyType);
			associationTypeMap.put(propertyName, AssociationType.ENTITY);

			ManyToOne mto = propertyField.getDeclaredAnnotation(ManyToOne.class);

			if (mto != null) {
				if (mto.fetch() == FetchType.EAGER) {
					nonLazyProperties.add(propertyName);
				}

				if (mto.optional()) {
					optionalAssociations.add(propertyName);
				}

				return;
			}

			OneToOne oto = propertyField.getDeclaredAnnotation(OneToOne.class);

			if (oto != null) {
				if (oto.fetch() == FetchType.EAGER) {
					nonLazyProperties.add(propertyName);
				}

				if (oto.optional()) {
					optionalAssociations.add(propertyName);
				}

				return;
			}
		}
		
		if (Collection.class.isAssignableFrom(propertyType)) {
			Class<?> genericType = (Class<?>) TypeHelper.getGenericType(propertyField);

			if (Entity.class.isAssignableFrom(genericType)) {
				associationClassMap.put(propertyName, (Class<? extends DomainEntity>) genericType);
				associationTypeMap.put(propertyName, AssociationType.COLLECTION);

				OneToMany otm = propertyField.getDeclaredAnnotation(OneToMany.class);

				if (otm != null && otm.fetch() == FetchType.EAGER) {
					nonLazyProperties.add(propertyName);
				}
				
				ManyToMany mtm = propertyField.getDeclaredAnnotation(ManyToMany.class);
				
				if (mtm != null && mtm.fetch() == FetchType.EAGER) {
					nonLazyProperties.add(propertyName);
				}
			}
		}
	}
	
	private void resolvePropertyLaziness(
			String propertyName,
			Type propertyType,
			int propertyIndex,
			boolean[] propertyLaziness,
			List<String> nonLazyProperties,
			Class<?> owningType) throws NoSuchFieldException, SecurityException {
		if (!org.hibernate.type.AssociationType.class.isAssignableFrom(propertyType.getClass())) {
			if (!propertyLaziness[propertyIndex]) {
				nonLazyProperties.add(propertyName);
				return;
			}
		}
		
		if (EntityType.class.isAssignableFrom(propertyType.getClass())) {
			if (((EntityType) propertyType).isEager(null)) {
				nonLazyProperties.add(propertyName);
			}
			
			return;
		}
		
		if (CollectionType.class.isAssignableFrom(propertyType.getClass())) {
			if (!Entity.class.isAssignableFrom((Class<?>) TypeHelper.getGenericType(owningType.getDeclaredField(propertyName)))) {
				if (!propertyLaziness[propertyIndex]) {
					nonLazyProperties.add(propertyName);
					return;
				}
			}
			// this is how a CollectionPersister role is resolved by Hibernate
			// see org.hibernate.cfg.annotations.CollectionBinder.bind()
			String collectionRole = StringHelper.qualify(owningType.getName(), propertyName);

			if (!HibernateHelper.getSessionFactory().getMetamodel().collectionPersister(collectionRole).isLazy()) {
				nonLazyProperties.add(propertyName);
			}

			return;
		}
		
		if (!propertyLaziness[propertyIndex]) {
			nonLazyProperties.add(propertyName);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void resolveAssociationMetadata(
			Class<?> owningType,
			String propertyName,
			Type propertyType,
			int propertyIndex,
			boolean[] nullabilities,
			Map<String, Class<? extends DomainEntity>> associationClassMap,
			Map<String, AssociationType> associationTypeMap,
			Set<String> optionalAssociations) throws NoSuchFieldException, SecurityException {
		if (!CollectionType.class.isAssignableFrom(propertyType.getClass())) {
			associationClassMap.put(propertyName, (Class<? extends DomainEntity>) propertyType.getReturnedClass());
			associationTypeMap.put(
					propertyName,
					EntityType.class.isAssignableFrom(propertyType.getClass()) ?
							AssociationType.ENTITY : AssociationType.ANY);

			if (nullabilities[propertyIndex]) {
				optionalAssociations.add(propertyName);
			}
			
			return;
		}
		
		Class<?> genericType = (Class<?>) TypeHelper
				.getGenericType(owningType.getDeclaredField(propertyName));
		
		if (DomainEntity.class.isAssignableFrom(genericType)) {
			associationClassMap.put(propertyName, (Class<? extends DomainEntity>) genericType);
			associationTypeMap.put(propertyName, AssociationType.COLLECTION);
			return;
		}
		
		throw new IllegalArgumentException(
				String.format("Unknown association type: [%s]", genericType.getName()));
	}
	// @formatter:on
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
	public Map<String, Getter> getGetters() {
		return getters;
	}

	@Override
	public Class<?> getPropertyType(String propertyName) {
		return propertyTypes.get(propertyName);
	}

	@Override
	public boolean isAssociation(String attributeName) {
		return associationClassMap.containsKey(attributeName);
	}

	@Override
	public boolean isAssociationOptional(String propertyName) {
		return optionalAssociations.contains(propertyName);
	}

	@Override
	public AssociationType getAssociationType(String associationName) {
		return associationTypeMap.get(associationName);
	}

	@Override
	public Class<? extends DomainEntity> getAssociationClass(String associationName) {
		return associationClassMap.get(associationName);
	}

	@Override
	public int getPropertiesSpan() {
		return properties.size();
	}

	@Override
	public String toString() {
		if (properties.isEmpty()) {
			return String.format("This %s is empty", this.getClass().getSimpleName());
		}
		// @formatter:off
		return String.format("%s(\n"
				+ "\tproperties|propertyTypes=[\n"
				+ "\t\t%s\n"
				+ "\t],\n"
				+ "\tnonLazyProperties=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ "\tassociationClasses=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ "\tassociationTypes=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ "\toptionalAssociations=[\n"
				+ "\t\t%s\n"
				+ "\t]\n"
				+ ")",
				this.getClass().getSimpleName(),
				IntStream.range(0, properties.size())
					.mapToObj(index -> String.format("%s:\t%s", properties.get(index), propertyTypes.get(properties.get(index)))).collect(Collectors.joining("\n\t\t")),
				nonLazyProperties.isEmpty() ? "<<empty>>" : nonLazyProperties.stream().collect(Collectors.joining(", ")),
				associationClassMap.isEmpty() ? "<<empty>>" : associationClassMap.entrySet().stream()
					.map(entry -> String.format("%s|%s", entry.getKey(), entry.getValue().getSimpleName())).collect(Collectors.joining(", ")),
				associationTypeMap.isEmpty() ? "<<empty>>" : associationTypeMap.entrySet().stream()
					.map(entry -> String.format("%s|%s", entry.getKey(), entry.getValue())).collect(Collectors.joining(", ")),
				optionalAssociations.isEmpty() ? "<<empty>>" :  optionalAssociations.stream().collect(Collectors.joining(", ")));
		// @formatter:on
	}

	@SuppressWarnings({ "rawtypes", "serial" })
	private class ComponentPropertyGetter implements Getter {

		private final Getter componentGetter;
		private final Getter componentPropertyGetter;

		private ComponentPropertyGetter(Getter componentGetter, Class<?> componentType, String propertyName,
				Class<?> propertyType) {
			this.componentGetter = Objects.requireNonNull(componentGetter);
			componentPropertyGetter = StandardAccess.locateGetter(componentType, propertyName)
					.orElseThrow(() -> new IllegalArgumentException(String.format(
							"Unable to locate getter for a Component's property. Component type: [%s], property type: [%s]",
							componentType.getName(), propertyName)));
		}

		@Override
		public Object get(Object owner) {
			return Optional.ofNullable(componentGetter.get(owner)).map(componentPropertyGetter::get).orElse(null);
		}

		@Override
		public Object getForInsert(Object owner, Map mergeMap, SharedSessionContractImplementor session) {
			return get(owner);
		}

		@Override
		public Class getReturnType() {
			return componentPropertyGetter.getReturnType();
		}

		@Override
		public Member getMember() {
			return componentPropertyGetter.getMember();
		}

		@Override
		public String getMethodName() {
			return componentPropertyGetter.getMethodName();
		}

		@Override
		public Method getMethod() {
			return componentPropertyGetter.getMethod();
		}

	}

}
