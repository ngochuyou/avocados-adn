/**
 * 
 */
package adn.service.resource.local;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.GeneratedValue;
import javax.persistence.SharedCacheMode;

import org.hibernate.EntityMode;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.SessionFactory;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.AttributeConverterInfo;
import org.hibernate.boot.CacheRegionDefinition;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.spi.ArchiveDescriptorFactory;
import org.hibernate.boot.internal.ClassLoaderAccessImpl;
import org.hibernate.boot.internal.ClassmateContext;
import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.SessionFactoryOptionsBuilder;
import org.hibernate.boot.model.IdGeneratorStrategyInterpreter;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.ObjectNameNormalizer;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.registry.internal.BootstrapServiceRegistryImpl;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.boot.spi.BasicTypeRegistration;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.MetadataSourceType;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.jpa.spi.MutableJpaCompliance;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.PropertyAccessStrategyResolver;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.internal.ProvidedService;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;
import org.jboss.jandex.IndexView;
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
import adn.service.Service;
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
		MetadataBuildingContext metadataBuildingContext = new MetadataBuildingContextImpl();
		
		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(metadataBuildingContext));
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
		contextBuildingService
				.getServiceWrapper(MetadataBuildingContext.class, wrapper -> wrapper.orElseThrow().unwrap())
				.getBootstrapContext().release();
	}

	private EntityManagerFactoryImplementor build(TypeConfiguration typeConfig)
			throws IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException {
		MetadataBuildingContext buildingContext = contextBuildingService
				.getServiceWrapper(MetadataBuildingContext.class, wrapper -> wrapper.orElseThrow().unwrap());

		return new ManagerFactory(contextBuildingService, typeConfig,
				buildingContext.getBuildingOptions().getServiceRegistry(),
				new SessionFactoryOptionsBuilder(buildingContext.getBuildingOptions().getServiceRegistry(),
						buildingContext.getBootstrapContext()));
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
			return instance;
		}

	}

	private final BootstrapServiceRegistry bootstrapService = new BootstrapServiceRegistryImpl();

	public class MetadataBuildingContextImpl implements MetadataBuildingContext, Service {

		private final BootstrapContext bootstrapContext = new BootstrapContextImpl();
		private final InFlightMetadataCollector metadataCollector = new InFlightMetadataCollectorImpl(bootstrapContext,
				getBuildingOptions());

		@Override
		public BootstrapContext getBootstrapContext() {
			return bootstrapContext;
		}

		@Override
		public MetadataBuildingOptions getBuildingOptions() {
			return bootstrapContext.getMetadataBuildingOptions();
		}

		@Override
		public MappingDefaults getMappingDefaults() {
			return bootstrapContext.getMetadataBuildingOptions().getMappingDefaults();
		}

		@Override
		public InFlightMetadataCollector getMetadataCollector() {
			return metadataCollector;
		}

		@Override
		public ClassLoaderAccess getClassLoaderAccess() {
			return bootstrapContext.getClassLoaderAccess();
		}

		@Override
		public ObjectNameNormalizer getObjectNameNormalizer() {

			return null;
		}

		public class BootstrapContextImpl implements BootstrapContext {

			private MetadataBuildingOptionsImpl options = new MetadataBuildingOptionsImpl();
			private ClassLoaderAccess classLoaderAccess = new ClassLoaderAccessImpl(
					locateSessionFactory().getServiceRegistry().requireService(ClassLoaderService.class));

			@Override
			public StandardServiceRegistry getServiceRegistry() {
				return locateServiceRegistry(StandardServiceRegistry.class);
			}

			@Override
			public MutableJpaCompliance getJpaCompliance() {
				return null;
			}

			@Override
			public TypeConfiguration getTypeConfiguration() {
				return locateSessionFactory().getMetamodel().getTypeConfiguration();
			}

			@Override
			public MetadataBuildingOptions getMetadataBuildingOptions() {
				return options;
			}

			@Override
			public boolean isJpaBootstrap() {

				return false;
			}

			@Override
			public void markAsJpaBootstrap() {

			}

			@Override
			public ClassLoader getJpaTempClassLoader() {

				return null;
			}

			@Override
			public ClassLoaderAccess getClassLoaderAccess() {
				return classLoaderAccess;
			}

			@Override
			public ClassmateContext getClassmateContext() {

				return null;
			}

			@Override
			public ArchiveDescriptorFactory getArchiveDescriptorFactory() {

				return null;
			}

			@Override
			public ScanOptions getScanOptions() {

				return null;
			}

			@Override
			public ScanEnvironment getScanEnvironment() {

				return null;
			}

			@Override
			public Object getScanner() {

				return null;
			}

			@Override
			public ReflectionManager getReflectionManager() {

				return null;
			}

			@Override
			public IndexView getJandexView() {

				return null;
			}

			@Override
			public Map<String, SQLFunction> getSqlFunctions() {
				return Collections.emptyMap();
			}

			@Override
			public Collection<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjectList() {
				return Collections.emptyList();
			}

			@Override
			public Collection<AttributeConverterInfo> getAttributeConverters() {

				return null;
			}

			@Override
			public Collection<CacheRegionDefinition> getCacheRegionDefinitions() {

				return null;
			}

			@Override
			public void release() {
				this.options = null;
				this.classLoaderAccess = null;
			}

		}

		public class MetadataBuildingOptionsImpl implements MetadataBuildingOptions {

			@SuppressWarnings("serial")
			private final StandardServiceRegistry serviceRegistry = new StandardServiceRegistryImpl(bootstrapService,
					Collections.emptyList(),
					// @formatter:off
					Arrays.asList(
						new ProvidedService<>(MutableIdentifierGeneratorFactory.class, contextBuildingService.getService(MutableIdentifierGeneratorFactory.class)),
						new ProvidedService<>(JdbcServices.class, locateSessionFactory().getJdbcServices()),
						new ProvidedService<>(JdbcEnvironment.class, locateSessionFactory().getJdbcServices().getJdbcEnvironment()),
						new ProvidedService<>(ConfigurationService.class, locateSessionFactory().getServiceRegistry().requireService(ConfigurationService.class)),
						new ProvidedService<>(RegionFactory.class, locateSessionFactory().getServiceRegistry().requireService(RegionFactory.class)),
						new ProvidedService<>(ProxyFactoryFactory.class, locateSessionFactory().getServiceRegistry().requireService(ProxyFactoryFactory.class)),
						new ProvidedService<>(PropertyAccessStrategyResolver.class, new PropertyAccessStrategyResolver() {					
							@Override
							public PropertyAccessStrategy resolvePropertyAccessStrategy(@SuppressWarnings("rawtypes") Class containerClass, String explicitAccessStrategyName,
									EntityMode entityMode) {
								return PropertyAccessStrategyFieldImpl.INSTANCE;
							}
						})
					),
					// @formatter:on
					Collections.emptyMap());
			private final MappingDefaults mappingDefaults = new MetadataBuilderImpl.MappingDefaultsImpl(
					getServiceRegistry());

			@Override
			public StandardServiceRegistry getServiceRegistry() {
				return serviceRegistry;
			}

			@Override
			public MappingDefaults getMappingDefaults() {
				return mappingDefaults;
			}

			@Override
			public List<BasicTypeRegistration> getBasicTypeRegistrations() {

				return null;
			}

			@Override
			public ReflectionManager getReflectionManager() {

				return null;
			}

			@Override
			public IndexView getJandexView() {

				return null;
			}

			@Override
			public ScanOptions getScanOptions() {

				return null;
			}

			@Override
			public ScanEnvironment getScanEnvironment() {

				return null;
			}

			@Override
			public Object getScanner() {

				return null;
			}

			@Override
			public ArchiveDescriptorFactory getArchiveDescriptorFactory() {

				return null;
			}

			@Override
			public ClassLoader getTempClassLoader() {

				return null;
			}

			@Override
			public ImplicitNamingStrategy getImplicitNamingStrategy() {

				return null;
			}

			@Override
			public PhysicalNamingStrategy getPhysicalNamingStrategy() {
				return PhysicalNamingStrategyStandardImpl.INSTANCE;
			}

			@Override
			public SharedCacheMode getSharedCacheMode() {

				return null;
			}

			@Override
			public AccessType getImplicitCacheAccessType() {

				return null;
			}

			@Override
			public MultiTenancyStrategy getMultiTenancyStrategy() {

				return null;
			}

			@Override
			public IdGeneratorStrategyInterpreter getIdGenerationTypeInterpreter() {

				return null;
			}

			@Override
			public List<CacheRegionDefinition> getCacheRegionDefinitions() {

				return null;
			}

			@Override
			public boolean ignoreExplicitDiscriminatorsForJoinedInheritance() {

				return false;
			}

			@Override
			public boolean createImplicitDiscriminatorsForJoinedInheritance() {

				return false;
			}

			@Override
			public boolean shouldImplicitlyForceDiscriminatorInSelect() {

				return false;
			}

			@Override
			public boolean useNationalizedCharacterData() {

				return false;
			}

			@Override
			public boolean isSpecjProprietarySyntaxEnabled() {

				return false;
			}

			@Override
			public boolean isNoConstraintByDefault() {

				return false;
			}

			@Override
			public List<MetadataSourceType> getSourceProcessOrdering() {

				return null;
			}

			@Override
			public Map<String, SQLFunction> getSqlFunctions() {

				return Collections.emptyMap();
			}

			@Override
			public List<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjectList() {
				return Collections.emptyList();
			}

			@Override
			public List<AttributeConverterInfo> getAttributeConverters() {
				return Collections.emptyList();
			}

		}

	}

	@SuppressWarnings("unchecked")
	private <T extends ServiceRegistry> T locateServiceRegistry(Class<T> type) {
		ServiceRegistry serviceResgistry = ContextProvider.getApplicationContext().getBean(SessionFactory.class)
				.unwrap(SessionFactoryImplementor.class).getServiceRegistry();

		if (ServiceRegistry.class.isAssignableFrom(type)) {
			return (T) serviceResgistry;
		}

		throw new ClassCastException(String.format("%s could not be casted to %s", ServiceRegistry.class, type));
	}

	private SessionFactoryImplementor locateSessionFactory() {
		return ContextProvider.getApplicationContext().getBean(SessionFactory.class)
				.unwrap(SessionFactoryImplementor.class);
	}

}
