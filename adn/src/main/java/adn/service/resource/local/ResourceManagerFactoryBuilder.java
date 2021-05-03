/**
 * 
 */
package adn.service.resource.local;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.GeneratedValue;

import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.application.context.ContextBuilder;
import adn.application.context.ContextProvider;
import adn.helpers.StringHelper;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor.ServiceWrapper;
import adn.service.resource.local.factory.ManagerFactory;
import adn.service.resource.metamodel.DefaultResourceIdentifierGenerator;
import adn.service.resource.storage.LocalResourceStorage;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;
import adn.service.resource.storage.ResultSetMetaDataImpl;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(6)
public class ResourceManagerFactoryBuilder implements ContextBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LocalResourceStorage localStorage;
	private ContextBuildingService contextBuildingService;

	public static final String MODEL_PACKAGE = "adn.service.resource.model.models";
	private Set<String> identifierGenerators;
	private Set<ManagerFactoryEventListener> eventListeners = new HashSet<>();

	@Override
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + "Building " + this.getClass());
		// @formatter:off
		logger.trace("\n\n"
				+ "\t\t\t\t\t\t========================================================\n"
				+ "\t\t\t\t\t\t=          BUILDING LOCAL RESOURCE MANAGEMENT          =\n"
				+ "\t\t\t\t\t\t========================================================\n");
		// @formatter:on
		// TODO Auto-generated method stub
		// register STATIC services
		// init identifierGenerators
		identifierGenerators = new HashSet<>();
		// create building service
		contextBuildingService = ContextBuildingService.createBuildingService();
		// obtain MutableIdentifierGeneratorFactory from Hibernate service
		SessionFactoryImplementor sfi = ContextProvider.getApplicationContext().getBean(SessionFactory.class)
				.unwrap(SessionFactoryImplementor.class);
		MutableIdentifierGeneratorFactory idGeneratorFactory = sfi.getServiceRegistry()
				.getService(MutableIdentifierGeneratorFactory.class);

		Assert.notNull(idGeneratorFactory, "Unable to locate IdentifierGeneratorFactory");
		contextBuildingService.register(MutableIdentifierGeneratorFactory.class, idGeneratorFactory);

		BasicTypeRegistry basicTypeRegistry = sfi.getMetamodel().getTypeConfiguration().getBasicTypeRegistry();

		Assert.notNull(basicTypeRegistry, "Unable to locate BasicTypeRegistry");
		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(basicTypeRegistry));

		Dialect dialect = sfi.getJdbcServices().getDialect();

		Assert.notNull(basicTypeRegistry, "Unable to locate Dialect");
		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(dialect));

		// register naming-strategy
		contextBuildingService.register(NamingStrategy.class, NamingStrategy.DEFAULT_NAMING_STRATEGY);
		contextBuildingService.register(LocalResourceStorage.class, localStorage);
		contextBuildingService.register(Metadata.class, new Metadata());
		Assert.notNull(ResultSetMetaDataImpl.INSTANCE,
				"Unable to locate instance of " + ResultSetMetaDataImplementor.class);
		contextBuildingService.register(ResultSetMetaDataImplementor.class, ResultSetMetaDataImpl.INSTANCE);

		FastSessionServices fsses = sfi.getFastSessionServices();

		Assert.notNull(fsses, "Unable to locate instance of " + FastSessionServices.class);

		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(fsses));
		// @formatter:off
		prepare();
		// @formatter:on
		// inject ResourceManager bean into ApplicationContext
		// usages of this bean should be obtained via
		// ContextProvider.getApplicationContext().getBean(ResourceManager.class.getName());
		// or
		// ContextProvider.getApplicationContext().getBean([Explicit bean name]);
		EntityManagerFactoryImplementor sessionFactory = build(sfi.getMetamodel().getTypeConfiguration());

		ContextProvider.getAccess().setLocalResourceSessionFactory(sessionFactory);
		postBuild();
		logger.info(getLoggingPrefix(this) + "Finished building " + this.getClass());
	}

	private void prepare() {
		importIdentifierGenerator();
		importDeclaredIdentifierGenerator();
		importResourceClass();
	}

	private void importResourceClass() {
		Metadata metadata = contextBuildingService.getService(Metadata.class);
		NamingStrategy namingStrategy = contextBuildingService.getService(NamingStrategy.class);
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(LocalResource.class));
		scanner.findCandidateComponents(MODEL_PACKAGE).forEach(bean -> {
			try {
				Class<?> clazz = Class.forName(bean.getBeanClassName());
				LocalResource anno = clazz.getDeclaredAnnotation(LocalResource.class);

				metadata.addImport(StringHelper.hasLength(anno.name()) ? anno.name() : namingStrategy.getName(clazz),
						clazz);
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		});
	}

	private void importIdentifierGenerator() {
		MutableIdentifierGeneratorFactory idGeneratorFactory = contextBuildingService
				.getService(MutableIdentifierGeneratorFactory.class);
		String defaultResourceIdentifierName = DefaultResourceIdentifierGenerator.NAME;

		idGeneratorFactory.register(defaultResourceIdentifierName, DefaultResourceIdentifierGenerator.class);
		identifierGenerators.add(defaultResourceIdentifierName);
	}

	private void importDeclaredIdentifierGenerator() {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		MutableIdentifierGeneratorFactory idGeneratorFactory = contextBuildingService
				.getService(MutableIdentifierGeneratorFactory.class);

		scanner.addIncludeFilter(new AnnotationTypeFilter(LocalResource.class));
		scanner.findCandidateComponents(MODEL_PACKAGE).stream().map(bean -> {
			try {
				return Class.forName(bean.getBeanClassName());
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
				return null;
			}
		}).forEach(clazz -> {
			try {
				for (Field f : clazz.getDeclaredFields()) {
					if (doesAutoGeneratorPresents(f)) {
						final GenericGenerator ggAnno = f.getDeclaredAnnotation(GenericGenerator.class);

						if (identifierGenerators.contains(ggAnno.strategy())) {
							logger.debug("Found " + ggAnno.strategy() + " on " + clazz.getName() + "." + f.getName());
							continue;
						}

						Class<?> generatorClass = Class.forName(ggAnno.strategy());

						if (!IdentifierGenerator.class.isAssignableFrom(generatorClass)) {
							throw new IllegalArgumentException(
									"Illegal IdentifierGenerator type: " + ggAnno.strategy());
						}

						idGeneratorFactory.register(ggAnno.strategy(), generatorClass);
						logger.debug("Registering IdentifierGenerator type: " + generatorClass);
					}
				}
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			} catch (IllegalArgumentException iae) {
				iae.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		});
	}

	private boolean doesAutoGeneratorPresents(Field f) {
		GeneratedValue gvAnno = f.getDeclaredAnnotation(GeneratedValue.class);
		GenericGenerator ggAnno = f.getDeclaredAnnotation(GenericGenerator.class);

		if (gvAnno == null || ggAnno == null) {
			return false;
		}

		if (!StringHelper.hasLength(ggAnno.name()) || !StringHelper.hasLength(ggAnno.strategy())
				|| ggAnno.parameters() == null) {
			throw new IllegalArgumentException("Invalid GenericGenerator, mandatory fields are empty");
		}

		return true;
	}

	public void addEventListener(ManagerFactoryEventListener listener) {
		if (eventListeners.contains(listener)) {
			throw new IllegalArgumentException("Duplicate event listener instance of type " + listener.getClass());
		}

		logger.trace("Adding new event listener of type " + listener.getClass());
		eventListeners.add(listener);
	}

	private void postBuild() {
		Assert.isTrue(
				Stream.of(identifierGenerators.toArray(String[]::new))
						.map(strategy -> contextBuildingService.getService(MutableIdentifierGeneratorFactory.class)
								.getIdentifierGeneratorClass(strategy) == null)
						.filter(ele -> ele).findAny().isEmpty(),
				"Failed to register IdentifierGenerators");
		eventListeners.forEach(listener -> listener.postBuild(null));
	}

	private EntityManagerFactoryImplementor build(TypeConfiguration typeConfig)
			throws IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		return new ManagerFactory(contextBuildingService, typeConfig);
	}

	public static void unsupport() {
		throw new UnsupportedOperationException("Some implementations might not be supported");
	}

	@SuppressWarnings("serial")
	public class ServiceWrapperImpl<T> implements ServiceWrapper<T> {

		private final T instance;

		public ServiceWrapperImpl(T instance) {
			// TODO Auto-generated constructor stub
			Assert.notNull(instance, "Cannot wrap a null instance in ServiceWrapper");
			this.instance = instance;
		}

		@Override
		public T unwrap() {
			// TODO Auto-generated method stub
			return instance;
		}

	}

}
