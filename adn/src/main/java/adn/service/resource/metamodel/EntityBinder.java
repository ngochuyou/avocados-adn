/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;
import java.sql.JDBCType;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.engine.OptimisticLockStyle;
import org.hibernate.id.ExportableColumn;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Subclass;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Identifier;
import org.hibernate.metamodel.model.domain.internal.SingularAttributeImpl.Version;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.type.BasicType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.local.ManagerFactoryEventListener;
import adn.service.resource.local.ResourcePersister;
import adn.service.resource.local.SharedIdentifierGeneratorFactory;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;
import javassist.Modifier;

/**
 * @author Ngoc Huy
 *
 */
public class EntityBinder implements ManagerFactoryEventListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static EntityBinder INSTANCE;
	private final SharedIdentifierGeneratorFactory identifierGeneratorfactory;

	public EntityBinder(SharedIdentifierGeneratorFactory identifierGeneratorfactory) {
		Assert.notNull(identifierGeneratorfactory, SharedIdentifierGeneratorFactory.class + " must not be null");

		this.identifierGeneratorfactory = identifierGeneratorfactory;

		listen();
	}

	@Override
	public void postBuild(EntityManagerFactoryImplementor sessionFactory) {
		// TODO Auto-generated method stub
		logger.trace("Cleaning up INSTANCE of type " + this.getClass().getName());
		EntityBinder.INSTANCE = null;
		logger = null;
	}

	public <X, T> IdentifierGenerator locateIdentifierGenerator(ResourceType<X> metamodel,
			EntityManagerFactoryImplementor sessionFactory) throws IllegalAccessException {
		return locateIdentifierGenerator(metamodel, metamodel.getIdType().getJavaType(), sessionFactory);
	}

	private <X, T> IdentifierGenerator locateIdentifierGenerator(ResourceType<X> metamodel, Class<T> identifierType,
			EntityManagerFactoryImplementor sessionFactory) throws IllegalAccessException {
		Assert.notNull(identifierType, "Identifier type must not be null");

		SingularPersistentAttribute<X, T> idAttribute = metamodel.getDeclaredId(identifierType);
		Field idField = ((Field) idAttribute.getJavaMember());
		GenericGenerator ggAnno = idField.getDeclaredAnnotation(GenericGenerator.class);

		Assert.notNull(ggAnno,
				"Unable to locate @GenericGenerator field " + metamodel.getJavaType() + "." + idField.getName());

		String generatorName = ggAnno.strategy();

		Assert.hasLength(generatorName, "Invalid IdentifierGenrator name");

		IdentifierGenerator generator = identifierGeneratorfactory.obtainGenerator(generatorName);

		generator = (generator != null ? generator
				: identifierGeneratorfactory.createIdentifierGenerator(generatorName, idAttribute.getJavaType()));
		Assert.notNull(generator, "Unable to locate IdentifierGenrator for " + idAttribute.getName());

		return generator;
	}

	public <D> Entry<EntityMetamodel, PersistentClass> buildRootClass(ResourcePersister<D> persister,
			ResourceType<D> metamodel, EntityManagerFactoryImplementor sessionFactory) {
		MetadataBuildingContext context = sessionFactory.getContextBuildingService()
				.getServiceWrapper(MetadataBuildingContext.class, (wrapper) -> wrapper.orElseThrow().unwrap());

		logger.trace(String.format("Building [%s] for JPA metamodel named [%s]", RootClass.class, metamodel.getName()));

		PersistentClass rootClass = new RootClass(context);

		return new Map.Entry<>() {
			public EntityMetamodel getKey() {
				return new EntityMetamodel(buildPersistentClass(persister, metamodel, rootClass, context), persister,
						sessionFactory);
			};

			public PersistentClass getValue() {
				return rootClass;
			}

			@Override
			public PersistentClass setValue(PersistentClass value) {
				return rootClass;
			}

		};

	}

	public <D> Entry<EntityMetamodel, PersistentClass> buildSubclass(ResourcePersister<D> persister,
			ResourceType<D> metamodel, RootClass root, EntityManagerFactoryImplementor sessionFactory) {
		logger.trace(String.format("Building [%s] for JPA metamodel named [%s]", Subclass.class, metamodel.getName()));

		MetadataBuildingContext context = sessionFactory.getContextBuildingService()
				.getServiceWrapper(MetadataBuildingContext.class, (wrapper) -> wrapper.orElseThrow().unwrap());
		Subclass subclass = new Subclass(root, context);

		root.addSubclass(subclass);

		return new Map.Entry<>() {
			public EntityMetamodel getKey() {
				return new EntityMetamodel(buildPersistentClass(persister, metamodel, subclass, context), persister,
						sessionFactory);
			};

			public PersistentClass getValue() {
				return subclass;
			}

			@Override
			public PersistentClass setValue(PersistentClass value) {
				return subclass;
			}

		};
	}

	private PersistentClass buildPersistentClass(ResourcePersister<?> persister, ResourceType<?> jpaModel,
			PersistentClass pc, MetadataBuildingContext context) {
		pc.setEntityName(jpaModel.getName());
		pc.setClassName(jpaModel.getJavaType().getName());
		pc.setJpaEntityName(jpaModel.getName());
		pc.setDiscriminatorValue(pc.getClassName());
		pc.setLazy(true);
		new Consumer<PersistentClass>() {
			@Override
			public void accept(PersistentClass pc) {
				if (pc instanceof RootClass) {
					buildDeclaredProperties(persister, jpaModel, pc, context);
					return;
				}

				@SuppressWarnings("unchecked")
				Iterator<Property> props = pc.getRootClass().getPropertyIterator();

				while (props.hasNext()) {
					pc.addProperty(props.next());
				}

				buildDeclaredProperties(persister, jpaModel, pc, context);
			}
		}.accept(pc);
		pc.setAbstract(Modifier.isAbstract(jpaModel.getJavaType().getModifiers()));
		pc.setCached(false);
		pc.setProxyInterfaceName(HibernateProxy.class.getName());
		pc.setOptimisticLockStyle(
				jpaModel.hasVersionAttribute() ? OptimisticLockStyle.VERSION : OptimisticLockStyle.DIRTY);

		if (pc instanceof RootClass) {
			RootClass root = (RootClass) pc;

			root.setIdentifierProperty(root.getProperty(persister.getIdentifierPropertyName()));
			root.setIdentifier((KeyValue) root.getIdentifierProperty().getValue());
			root.setVersion(jpaModel.hasVersionAttribute()
					? root.getProperty(persister.getPropertyNames()[persister.getVersionProperty()])
					: null);
			root.setLazyPropertiesCacheable(false);

			SimpleValue discriminatorValue = new SimpleValue(context, null);
			Column col = new Column("DTYPE");

			col.setValue(discriminatorValue);
			col.setSqlType(JDBCType.valueOf(StringType.INSTANCE.getSqlTypeDescriptor().getSqlType()).getName());
			discriminatorValue.addColumn(col);
			root.setDiscriminator(discriminatorValue);
		}

		return pc;
	}

	private void buildDeclaredProperties(ResourcePersister<?> persister, ResourceType<?> jpaModel, PersistentClass pc,
			MetadataBuildingContext context) {
		jpaModel.getDeclaredAttributes().stream().forEach(attr -> {
			Property prop = new Property();

			prop.setName(attr.getName());
			new Consumer<Property>() {
				@Override
				public void accept(Property t) {
					if (attr instanceof Identifier || attr instanceof Version) {
						SimpleValue keyVal = new SimpleValue(context, null);

						keyVal.setTypeName(((SingularPersistentAttribute<?, ?>) attr).getType().getTypeName());
						prop.setValue(keyVal);

						return;
					}

					prop.setValue(new ExportableColumn.ValueImpl(null, null,
							(BasicType) persister.getPropertyType(attr.getName()), null));
				}
			}.accept(prop);
			prop.setCascade("none");
			prop.setUpdateable(persister.getPropertyUpdateability()[persister.getPropertyIndex(attr.getName())]);
			prop.setInsertable(true);
			prop.setSelectable(true);
			prop.setOptimisticLocked(true);
			prop.setValueGenerationStrategy(persister.getValueGeneration(attr.getName()));
			prop.setPropertyAccessorName(persister.getPropertyAccess(attr.getName()).getClass().getName());
			prop.setOptional(persister.getPropertyNullability()[persister.getPropertyIndex(attr.getName())]);
			prop.setPersistentClass(pc);
			prop.setNaturalIdentifier(false);
			prop.setLob(false);

			pc.addProperty(prop);
		});
	}

}
