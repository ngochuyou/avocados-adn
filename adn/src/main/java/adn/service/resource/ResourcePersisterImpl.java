package adn.service.resource;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Optional;
import java.util.function.Consumer;

import javax.persistence.metamodel.Attribute;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.internal.SessionCreationOptions;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.helpers.FunctionHelper;
import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;
import adn.helpers.StringHelper;
import adn.helpers.Utils;
import adn.service.resource.annotation.Constructor;
import adn.service.resource.annotation.Content;
import adn.service.resource.annotation.Directory;
import adn.service.resource.annotation.Extension;
import adn.service.resource.connection.LocalStorageConnection;
import adn.service.resource.engine.access.DirectAccess;
import adn.service.resource.engine.access.LiterallyNamedAccess;
import adn.service.resource.engine.access.PropertyAccessDelegate;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.access.StandardAccess;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.template.ResourceTemplateImpl;
import adn.service.resource.engine.tuple.InstantiatorFactory;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;
import adn.service.resource.factory.EntityManagerFactoryImplementor;
import adn.service.resource.factory.ManagerFactory;
import adn.service.resource.type.AbstractExplicitlyBindedType;
import adn.service.resource.type.NoOperationSet;

/**
 * @author Ngoc Huy
 *
 * @param <D>
 */
public class ResourcePersisterImpl<D> extends SingleTableEntityPersister
		implements ResourcePersister<D>, ClassMetadata, SharedSessionUnwrapper, SessionFactoryObserver {

	private static final long serialVersionUID = 1L;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ResourcePersisterImpl(PersistentClass persistentClass, EntityDataAccess cacheAccessStrategy,
			NaturalIdDataAccess naturalIdRegionAccessStrategy, PersisterCreationContext creationContext)
			throws HibernateException, IllegalAccessException, NoSuchFieldException, SecurityException {
		super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext);

		creationContext.getSessionFactory().addObserver(this);
	}

	@Override
	public void sessionFactoryCreated(SessionFactory factory) {
		SessionCreationOptions sessionCreationOptions = ResourceSession.getSessionCreationOptions();
		Connection delegate = sessionCreationOptions.getConnection();

		if (delegate instanceof LocalStorageConnection) {
			Attribute<? super D, ?> contentAttr = locateContentProperty();
			int span = contentAttr == null ? getEntityMetamodel().getPropertySpan() + 2
					: getEntityMetamodel().getPropertySpan() + 1;
			String[] columnNames = new String[span];
			Class<?>[] columnTypes = new Class<?>[span];
			PropertyAccessImplementor[] accessors = new PropertyAccessImplementor[span];
			PojoInstantiator<File> instantiator = determineInstantiator();
			boolean[] columnNullabilities = new boolean[span];

			processPathColumn(columnNames, columnTypes, accessors, columnNullabilities);

			int contentIndex = processContentColumn(contentAttr, columnNames, columnTypes, columnNullabilities,
					accessors);
			int extensionIndex = processExtensionColumn(columnNames, columnTypes, columnNullabilities, accessors);
			int arrayBound = contentAttr == null ? span - 2 : span - 1;
			String propertyName;
			String propertyColumnName;

			for (int i = 0, j = 3; i < arrayBound; i++) {
				if (i == contentIndex) {
					j++;
					continue;
				}

				if (i == extensionIndex) {
					continue;
				}

				propertyName = getPropertyNames()[i];
				propertyColumnName = getPropertyColumnNames(i)[0];
				columnNames[j] = StringHelper.hasLength(propertyColumnName) ? propertyColumnName : propertyName;
				columnTypes[j] = getPropertyType(propertyName).getReturnedClass();
				accessors[j] = determinePropertyAccess(getPropertyType(propertyName), propertyName);
				columnNullabilities[j] = getEntityMetamodel().getPropertyNullability()[i];
				j++;
			}

			LocalStorageConnection connection = (LocalStorageConnection) delegate;
			// @formatter:off
			logger.trace(String.format("Registering template [%s]", getEntityName()));
			connection.getStorage().registerTemplate(String.format("%s%s%s", getTableName(), ManagerFactory.DTYPE_SEPERATOR, StringHelper.get((String) getDiscriminatorValue()).orElse("")),
					determineDirectoryName(getMappedClass()),
					columnNames,
					columnTypes,
					columnNullabilities,
					accessors,
					instantiator);
			// @formatter:on
			return;
		}

		throw new IllegalArgumentException(String.format(
				"Unable to register resource template [%s] due to connection type mismatch, expect connection of type [%s]",
				getEntityName(), LocalStorageConnection.class));
	}

	/**
	 * @see File#getName()
	 */
	private static final String FILE_NAME_FIELD_NAME = "name";

	private void processPathColumn(String[] columnNames, Class<?>[] columnTypes, PropertyAccessImplementor[] accessors,
			boolean[] columnNullabilities) {
		int defaultPathColumnIndex = ResourceTemplateImpl.DEFAULT_PATH_INDEX;

		columnNames[defaultPathColumnIndex] = getIdentifierColumnNames()[0];
		columnTypes[defaultPathColumnIndex] = getIdentifierType().getReturnedClass();
		columnNullabilities[defaultPathColumnIndex] = false;

		org.springframework.data.annotation.AccessType.Type accessTypeAnno = locateAnnotatedAccessType(
				columnNames[defaultPathColumnIndex]);

		if (accessTypeAnno == null) {
			logger.trace(String.format("Using default path access strategy on identifier [%s]",
					columnNames[defaultPathColumnIndex]));
			accessors[defaultPathColumnIndex] = PropertyAccessStrategyFactory.DELEGATED_ACCESS_STRATEGY
					.buildPropertyAccess(File.class, FILE_NAME_FIELD_NAME, String.class);

			return;
		}

		accessors[defaultPathColumnIndex] = determinePropertyAccess(
				getPropertyType(columnNames[defaultPathColumnIndex]), columnNames[defaultPathColumnIndex]);
	}

	private int processExtensionColumn(String[] columnNames, Class<?>[] columnTypes, boolean[] columnNullabilities,
			PropertyAccessImplementor[] accessors) {
		Attribute<? super D, ?> extensionAttr = locateExtensionProperty();

		if (extensionAttr == null) {
			throw new IllegalArgumentException(
					String.format("Unable to locate extension info of [%s]", getEntityName()));
		}

		final int defaultExtensionIndex = ResourceTemplateImpl.DEFAULT_EXTENSION_INDEX;
		int extensionIndex = getPropertyIndex(extensionAttr.getName());
		String propertyColumnName = getPropertyColumnNames(extensionIndex)[0];

		columnNames[defaultExtensionIndex] = StringHelper.hasLength(propertyColumnName) ? propertyColumnName
				: extensionAttr.getName();
		columnTypes[defaultExtensionIndex] = getPropertyTypes()[extensionIndex].getReturnedClass();
		accessors[defaultExtensionIndex] = determinePropertyAccess(getPropertyType(extensionAttr.getName()),
				extensionAttr.getName());

		logger.trace(String.format("Found extension property [%s]: [%s] at index [%d]", extensionAttr.getName(),
				columnTypes[defaultExtensionIndex], extensionIndex));
		columnNullabilities[defaultExtensionIndex] = false;

		return extensionIndex;
	}

	private int processContentColumn(Attribute<? super D, ?> contentAttr, String[] columnNames, Class<?>[] columnTypes,
			boolean[] columnNullabilities, PropertyAccessImplementor[] accessors) {
		String propertyColumnName;
		final int defaultContentIndex = ResourceTemplateImpl.DEFAULT_CONTENT_INDEX;

		if (contentAttr != null) {
			int contentIndex = getPropertyIndex(contentAttr.getName());

			propertyColumnName = getPropertyColumnNames(contentIndex)[0];
			columnNames[defaultContentIndex] = StringHelper.hasLength(propertyColumnName) ? propertyColumnName
					: contentAttr.getName();
			columnTypes[defaultContentIndex] = getPropertyTypes()[contentIndex].getReturnedClass();
			accessors[defaultContentIndex] = determinePropertyAccess(getPropertyType(contentAttr.getName()),
					contentAttr.getName());

			logger.trace(String.format("Found content property [%s]: [%s] at index [%d]", contentAttr.getName(),
					columnTypes[defaultContentIndex], contentIndex));
			columnNullabilities[defaultContentIndex] = false;

			return contentIndex;
		}

		columnNames[defaultContentIndex] = ResourceTemplate.NO_CONTENT.toString();
		columnTypes[defaultContentIndex] = ResourceTemplate.NO_CONTENT.getClass();
		accessors[defaultContentIndex] = PropertyAccessStrategyFactory.NO_ACCESS_STRATEGY.buildPropertyAccess(null,
				null);
		columnNullabilities[defaultContentIndex] = true;

		return -1;
	}

	@SuppressWarnings("unchecked")
	private PojoInstantiator<File> determineInstantiator() {
		if (!getMappedClass().isAnnotationPresent(Constructor.class)) {
			throw new IllegalArgumentException(
					String.format("[%s] is required on [%s]", Constructor.class, getMappedClass().getName()));
		}

		Constructor anno = (Constructor) getMappedClass().getDeclaredAnnotation(Constructor.class);

		if (anno.columnNames().length == 0) {
			return InstantiatorFactory.buildDefault(File.class);
		}

		return InstantiatorFactory.build(File.class, anno.columnNames(), anno.argumentTypes());
	}

	@SuppressWarnings("unchecked")
	private String determineDirectoryName(Class<?> mappedClass) {
		if (mappedClass == null) {
			return "";
		}

		if (!mappedClass.isAnnotationPresent(Directory.class)) {
			return determineDirectoryName(mappedClass.getSuperclass());
		}

		Directory anno = (Directory) getMappedClass().getDeclaredAnnotation(Directory.class);

		return anno.path();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <F, S, R, E extends RuntimeException> PropertyAccessImplementor determinePropertyAccess(Type propertyType,
			String propertyName) {
		org.springframework.data.annotation.AccessType.Type accessTypeAnno = locateAnnotatedAccessType(propertyName);

		if (accessTypeAnno != org.springframework.data.annotation.AccessType.Type.PROPERTY
				&& !(propertyType instanceof AbstractExplicitlyBindedType)) {
			// @formatter:off
			Getter getter = Optional.ofNullable(StandardAccess.locateGetter(File.class, propertyName)
										.orElse(DirectAccess.locateGetter(File.class, propertyName)
												.orElse(LiterallyNamedAccess.locateGetter(File.class, propertyName)
														.orElse(null))))
									.orElseThrow(() -> new IllegalArgumentException(String.format("Unable to locate getter for property [%s]", propertyName)));
			Setter setter = Optional.ofNullable(StandardAccess.locateSetter(File.class, propertyName)
										.orElse(DirectAccess.locateSetter(File.class, propertyName)
												.orElse(!(getter instanceof LiterallyNamedAccess) ? LiterallyNamedAccess.locateSetter(File.class, propertyName)
														.orElse(null) : null)))
									.orElseThrow(() -> new IllegalArgumentException(String.format("Unable to locate setter for property [%s]", propertyName)));
			// @formatter:on
			return PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY.buildPropertyAccess(getter, setter);
		}

		Setter setter = propertyType instanceof NoOperationSet ? null
				: PropertyAccessDelegate.locateSetter(File.class, propertyName, false, propertyType.getReturnedClass())
						.orElse(null);
		Getter getter = PropertyAccessDelegate.locateGetter(File.class, propertyName, false).orElse(null);

		if (!(propertyType instanceof AbstractExplicitlyBindedType)) {
			return PropertyAccessStrategyFactory.SPECIFIC_ACCESS_STRATEGY.buildPropertyAccess(getter, setter);
		}

		Utils.Entry<Object, Object> lamdaEntry = locatePropertyAccessLambda(
				(AbstractExplicitlyBindedType<?>) propertyType);
		Object getterLambda = lamdaEntry.key;
		Object setterLambda = lamdaEntry.value;

		if (setter == null && getter == null) {
			if (getterLambda == null && setterLambda == null) {
				throw new IllegalArgumentException(String
						.format("Unable to locate access for property [%s] in type [%s]", propertyName, File.class));
			}

			if (getterLambda != null && setterLambda != null
					&& !getterLambda.getClass().equals(setterLambda.getClass())) {
				return PropertyAccessStrategyFactory.createMixedAccess().buildPropertyAccess(getterLambda,
						setterLambda);
			}

			if (HandledFunction.class.isAssignableFrom(getterLambda.getClass())) {
				return PropertyAccessStrategyFactory.createFunctionalAccess()
						.buildPropertyAccess((HandledFunction) getterLambda, (HandledFunction) setterLambda);
			}

			if (HandledBiFunction.class.isAssignableFrom(getterLambda.getClass())) {
				return PropertyAccessStrategyFactory.createBiFunctionalAccess()
						.buildPropertyAccess((HandledBiFunction) getterLambda, (HandledBiFunction) setterLambda);
			}

			if (HandledConsumer.class.isAssignableFrom(getterLambda.getClass())) {
				return PropertyAccessStrategyFactory.createConsumingAccess()
						.buildPropertyAccess((HandledConsumer) getterLambda, (HandledConsumer) setterLambda);
			}

			return PropertyAccessStrategyFactory.createSupplyingAccess()
					.buildPropertyAccess((HandledSupplier) getterLambda, (HandledSupplier) setterLambda);
		}
		// @formatter:off
		return PropertyAccessStrategyFactory.createHybridAccess().buildPropertyAccess(
					getter,
					setter,
					getterLambda,
					setterLambda);
		// @formatter:on
	}

	@SuppressWarnings("rawtypes")
	private Utils.Entry<Object, Object> locatePropertyAccessLambda(AbstractExplicitlyBindedType<?> type) {
		Method[] methods = type.getClass().getDeclaredMethods();
		Utils.Entry<Object, Object> entry = new Utils.Entry<>(null, null);
		Consumer<Object> consumer = (o) -> entry.key = o;

		for (Method method : methods) {
			adn.service.resource.engine.access.PropertyAccess pa = method
					.getDeclaredAnnotation(adn.service.resource.engine.access.PropertyAccess.class);

			if (pa == null) {
				continue;
			}

			consumer = (o) -> entry.key = o;

			adn.service.resource.engine.access.PropertyAccess.Type accessType = pa.type();

			if (accessType == adn.service.resource.engine.access.PropertyAccess.Type.SETTER) {
				consumer = (o) -> entry.value = o;
			}

			Class<?> accessClazz = pa.clazz();

			Assert.isTrue(accessClazz != null && accessType != null, "Access type and access class must not be null");

			if (HandledFunction.class.isAssignableFrom(accessClazz)) {
				consumer.accept(FunctionHelper.from((HandledFunction) type));
				continue;
			}

			if (HandledBiFunction.class.isAssignableFrom(accessClazz)) {
				consumer.accept(FunctionHelper.from((HandledBiFunction) type));
				continue;
			}

			if (HandledConsumer.class.isAssignableFrom(accessClazz)) {
				consumer.accept(FunctionHelper.from((HandledConsumer) type));
				continue;
			}

			if (HandledSupplier.class.isAssignableFrom(accessClazz)) {
				consumer.accept(FunctionHelper.from((HandledSupplier) type));
				continue;
			}
		}

		return entry;
	}

	private org.springframework.data.annotation.AccessType.Type locateAnnotatedAccessType(String name) {
		Attribute<? super D, ?> attribute = getFactory().getMetamodel().entity(getEntityName()).getAttribute(name);
		Field member;

		if ((member = (Field) attribute.getJavaMember())
				.isAnnotationPresent(org.springframework.data.annotation.AccessType.class)) {
			return member.getDeclaredAnnotation(org.springframework.data.annotation.AccessType.class).value();
		}

		return null;
	}

	private Attribute<? super D, ?> locateExtensionProperty() {
		return getFactory().getMetamodel().entity(getEntityName()).getAttributes().stream().filter(attr -> {
			if (attr.getJavaMember() instanceof Field) {
				return ((Field) attr.getJavaMember()).isAnnotationPresent(Extension.class);
			}

			return false;
		}).findFirst().orElse(null);
	}

	private Attribute<? super D, ?> locateContentProperty() {
		return getFactory().getMetamodel().entity(getEntityName()).getAttributes().stream().filter(attr -> {
			if (attr.getJavaMember() instanceof Field) {
				return ((Field) attr.getJavaMember()).isAnnotationPresent(Content.class);
			}

			return false;
		}).findFirst().orElse(null);
	}

	@Override
	public EntityManagerFactoryImplementor getFactory() {
		return (EntityManagerFactoryImplementor) super.getFactory();
	}

	@Override
	public ResourcePersister<D> getEntityPersister() {
		return this;
	}

}
