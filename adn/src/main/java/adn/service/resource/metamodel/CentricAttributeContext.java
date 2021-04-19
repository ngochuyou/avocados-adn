/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;

import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.domain.internal.BasicTypeImpl;
import org.hibernate.metamodel.model.domain.internal.PluralAttributeBuilder;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Identifier;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Version;
import org.hibernate.metamodel.model.domain.spi.BasicTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.service.Service;
import org.hibernate.type.ArrayType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.CollectionType;
import org.hibernate.type.MapType;
import org.hibernate.type.ObjectType;
import org.hibernate.type.SetType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.service.resource.metamodel.PropertyBinder.KeyValueContext;

/**
 * @author Ngoc Huy
 *
 */
public interface CentricAttributeContext extends Service {

	MetamodelImplementor getMetamodelImplementor();

	BasicTypeRegistry getTypeRegistry();

	PersistentAttributeType determinePersistentAttributeType(Class<?> type);

	<D> Type resolveType(ResourceType<D> owner, Attribute<D, ?> f);

	<J> BasicTypeDescriptor<J> resolveTypeDescriptor(Class<J> type);

	<D, T> SingularPersistentAttribute<D, T> createIdentifier(ResourceType<D> owner, Field f);

	<D, T> SingularPersistentAttribute<D, T> createVersion(ResourceType<D> owner, Field f);

	<D, T> SingularPersistentAttribute<D, T> createSingularAttribute(ResourceType<D> owner, Field f,
			boolean isOptional);

	<D, C, E> PluralPersistentAttribute<D, C, E> createPluralAttribute(ResourceType<D> owner, Field f);

	boolean isPlural(Class<?> type);

	boolean isAny(Class<?> type);

	boolean isBasic(Class<?> type);

	@SuppressWarnings("serial")
	public class CentricAttributeContextImpl implements CentricAttributeContext {

		private final Logger logger = LoggerFactory.getLogger(this.getClass());

		private final BasicTypeRegistry typeRegistry;
		private final MetamodelImplementor metamodel;

		private final Map<String, BasicTypeDescriptor<?>> basicTypeDescriptorMap = new HashMap<>();

		public CentricAttributeContextImpl(BasicTypeRegistry typeRegistry, MetamodelImplementor metamodel) {
			// TODO Auto-generated constructor stub
			this.typeRegistry = typeRegistry;
			this.metamodel = metamodel;
		}

		@Override
		public PersistentAttributeType determinePersistentAttributeType(Class<?> type) {
			// TODO Auto-generated method stub
			if (!isPlural(type)) {
				return PersistentAttributeType.BASIC;
			}

			return Optional.ofNullable(isPlural(type) ? PersistentAttributeType.ELEMENT_COLLECTION : null)
					.orElseThrow(() -> new IllegalArgumentException(
							"Unable to determine PersistentAttributeType of type " + type.getName()));
		}

		@Override
		public <D> Type resolveType(ResourceType<D> owner, Attribute<D, ?> f) {
			// TODO Auto-generated method stub
			if (!isPlural(f.getJavaType())) {
				if (isAny(f.getJavaType())) {
					return ObjectType.INSTANCE;
				}

				return typeRegistry.getRegisteredType(f.getJavaType().getName());
			}

			return resolveCollectionType(owner, f.getJavaType(), f.getName());
		}

		@Override
		public boolean isAny(Class<?> type) {
			return !isPlural(type) && !isBasic(type);
		}

		@Override
		public boolean isPlural(Class<?> type) {
			return Collection.class.isAssignableFrom(type);
		}

		@Override
		public boolean isBasic(Class<?> type) {
			return typeRegistry.getRegisteredType(type.getName()) != null;
		}

		private <D> CollectionType resolveCollectionType(ResourceType<D> owner, Class<?> type, String propName) {
			if (Set.class.isAssignableFrom(type)) {
				return new SetType(owner.getName() + "." + propName, propName);
			}

			if (List.class.isAssignableFrom(type)) {
				return new SetType(owner.getName() + "." + propName, propName);
			}

			if (type.isArray()) {
				return new ArrayType(owner.getName() + "." + propName, propName, type.getComponentType());
			}

			if (Map.class.isAssignableFrom(type)) {
				return new MapType(owner.getName() + "." + propName, propName);
			}

			throw new IllegalArgumentException("Unable to resolve CollectionType " + type.getName());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <J> BasicTypeDescriptor<J> resolveTypeDescriptor(Class<J> type) {
			// TODO Auto-generated method stub
			String typeName = type.getName();

			if (basicTypeDescriptorMap.containsKey(typeName)) {
				BasicTypeDescriptor<?> candidate = basicTypeDescriptorMap.get(typeName);

				if (candidate.getJavaType().equals(type)) {
					return (BasicTypeDescriptor<J>) candidate;
				}

				throw new IllegalArgumentException(String.format(
						"Unable to locate BasicTypeDescriptor due to type confliction. Required type %s, found type %s",
						type, candidate.getJavaType()));
			}

			return addType(new BasicTypeImpl<>(type, null));
		}

		private <J> BasicTypeDescriptor<J> addType(BasicTypeDescriptor<J> newType) {
			String typeName = newType.getJavaType().getName();

			if (basicTypeDescriptorMap.containsKey(typeName)) {
				logger.trace(
						String.format("Ignoring BasicType contribution: [%s, %s]", typeName, newType.getJavaType()));

				return newType;
			}

			basicTypeDescriptorMap.put(typeName, (BasicTypeDescriptor<?>) newType);
			logger.trace(String.format("New BasicType contribution: [%s, %s]", typeName, newType.getJavaType()));

			return newType;
		}

		@Override
		public BasicTypeRegistry getTypeRegistry() {
			// TODO Auto-generated method stub
			return typeRegistry;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <D, T> SingularPersistentAttribute<D, T> createIdentifier(ResourceType<D> owner, Field f) {
			// TODO Auto-generated method stub
			return new Identifier<>(owner, f.getName(), resolveTypeDescriptor((Class<T>) f.getType()), f,
					PersistentAttributeType.BASIC);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <D, T> SingularPersistentAttribute<D, T> createVersion(ResourceType<D> owner, Field f) {
			// TODO Auto-generated method stub
			return new Version<>(owner, f.getName(), PersistentAttributeType.BASIC,
					resolveTypeDescriptor((Class<T>) f.getType()), f);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <D, T> SingularPersistentAttribute<D, T> createSingularAttribute(ResourceType<D> owner, Field f,
				boolean isOptional) {
			// TODO Auto-generated method stub
			return new SingularAttributeImpl<>(owner, f.getName(), PersistentAttributeType.BASIC,
					resolveTypeDescriptor((Class<T>) f.getType()), f, false, false, isOptional);
		}

		@Override
		public <D, C, E> PluralPersistentAttribute<D, C, E> createPluralAttribute(ResourceType<D> owner, Field f) {
			// TODO Auto-generated method stub
			if (!Collection.class.isAssignableFrom(f.getType())) {
				throw new IllegalArgumentException("PluralAttribute describes Collection property only");
			}

			@SuppressWarnings("unchecked")
			Class<C> collectionType = (Class<C>) f.getType();
			PluralAttributeBuilder<D, C, E, ?> builder;

			if (collectionType.equals(Map.class)) {
				KeyValueContext<E, ?> kvPair = PropertyBinder.INSTANCE.determineMapGenericType(f);

				builder = new PluralAttributeBuilder<>(owner, resolveTypeDescriptor(kvPair.keyType), collectionType,
						resolveTypeDescriptor(kvPair.valueType));

			} else {
				builder = new PluralAttributeBuilder<>(owner,
						resolveTypeDescriptor(PropertyBinder.INSTANCE.determineNonMapGenericType(f)), collectionType,
						null);
			}

			Property prop = new Property();

			prop.setName(f.getName());
			builder.property(prop);
			builder.member(f);
			builder.persistentAttributeType(PersistentAttributeType.BASIC);

			return builder.build();
		}

		@Override
		public MetamodelImplementor getMetamodelImplementor() {
			// TODO Auto-generated method stub
			return metamodel;
		}

	}

}
