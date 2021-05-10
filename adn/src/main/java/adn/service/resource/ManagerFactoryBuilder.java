/**
 * 
 */
package adn.service.resource;

import java.util.Arrays;
import java.util.Collections;

import org.hibernate.EntityMode;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.CfgXmlAccessService;
import org.hibernate.boot.internal.SessionFactoryOptionsBuilder;
import org.hibernate.boot.model.process.spi.MetadataBuildingProcess;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.internal.BootstrapServiceRegistryImpl;
import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.mapping.Property;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.PropertyAccessStrategyResolver;
import org.hibernate.service.internal.ProvidedService;
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
import adn.service.resource.factory.BootstrapContextImpl;
import adn.service.resource.factory.DefaultResourceIdentifierGenerator;
import adn.service.resource.factory.EntityManagerFactoryImplementor;
import adn.service.resource.factory.ManagerFactory;
import adn.service.resource.factory.MetadataBuildingOptionsImpl;
import adn.service.resource.metamodel.type.FileCreationTimeStampType;
import adn.service.resource.model.models.FileResource;
import adn.service.resource.storage.LocalResourceStorage;
import adn.service.resource.storage.LocalResourceStorage.ResultSetMetaDataImplementor;
import adn.service.resource.storage.ResultSetMetaDataImpl;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(6)
public class ManagerFactoryBuilder implements ContextBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String MODEL_PACKAGE = "adn.service.resource.model.models";

	private MetadataSources metadataSources;

	private BootstrapServiceRegistry bootstrapServiceRegistry;
	private StandardServiceRegistry serviceRegistry;
	private MetadataBuildingOptions metadataBuildingOptions;
	private BootstrapContext bootstrapContext;

	@Autowired
	private LocalResourceStorage localStorage;

	@SuppressWarnings("serial")
	@Override
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + "Building " + this.getClass());
		// @formatter:off
		logger.trace("\n\n"
				+ "\t\t\t\t\t\t========================================================\n"
				+ "\t\t\t\t\t\t=          BUILDING LOCAL RESOURCE MANAGEMENT          =\n"
				+ "\t\t\t\t\t\t========================================================\n");
		// @formatter:on
		// intentionally not using @Autowired so that sf and sfi references would be
		// released post-build
		SessionFactory sf = ContextProvider.getApplicationContext().getBean(SessionFactory.class);
		SessionFactoryImplementor sfi = sf.unwrap(SessionFactoryImplementor.class);

		logger.trace(String.format("Instantiating [%s]", BootstrapServiceRegistryImpl.class));
		bootstrapServiceRegistry = new BootstrapServiceRegistryImpl();
		logger.trace(String.format("Instantiating [%s]", StandardServiceRegistryImpl.class));
		// @formatter:off
		MutableIdentifierGeneratorFactory migf = sfi.getServiceRegistry().requireService(MutableIdentifierGeneratorFactory.class);
		
		migf.register(DefaultResourceIdentifierGenerator.NAME, DefaultResourceIdentifierGenerator.class);
		serviceRegistry = new StandardServiceRegistryImpl(
				bootstrapServiceRegistry,
				Collections.emptyList(),
				Arrays.asList(
					new ProvidedService<>(MutableIdentifierGeneratorFactory.class, migf),
					new ProvidedService<>(JdbcServices.class, sfi.getJdbcServices()),
					new ProvidedService<>(JdbcEnvironment.class, sfi.getJdbcServices().getJdbcEnvironment()),
					new ProvidedService<>(ConfigurationService.class, sfi.getServiceRegistry().requireService(ConfigurationService.class)),
					new ProvidedService<>(RegionFactory.class, sfi.getServiceRegistry().requireService(RegionFactory.class)),
					new ProvidedService<>(ProxyFactoryFactory.class, sfi.getServiceRegistry().requireService(ProxyFactoryFactory.class)),
					new ProvidedService<>(CfgXmlAccessService.class, sfi.getServiceRegistry().requireService(CfgXmlAccessService.class)),
					new ProvidedService<>(CacheImplementor.class, sfi.getServiceRegistry().requireService(CacheImplementor.class)),
					new ProvidedService<>(PersisterFactory.class, sfi.getServiceRegistry().requireService(PersisterFactory.class)),
					new ProvidedService<>(ResultSetMetaDataImplementor.class, ResultSetMetaDataImpl.INSTANCE),
					new ProvidedService<>(PropertyAccessStrategyResolver.class, new PropertyAccessStrategyResolver() {					
						@Override
						public PropertyAccessStrategy resolvePropertyAccessStrategy(@SuppressWarnings("rawtypes") Class containerClass, String explicitAccessStrategyName,
								EntityMode entityMode) {
							return PropertyAccessStrategyFieldImpl.INSTANCE;
						}
					})
				),
				Collections.emptyMap());
		// @formatter:on
		metadataBuildingOptions = new MetadataBuildingOptionsImpl(serviceRegistry);
		bootstrapContext = new BootstrapContextImpl(serviceRegistry, metadataBuildingOptions);
		((MetadataBuildingOptionsImpl) metadataBuildingOptions).makeReflectionManager(bootstrapContext);

		build(sfi.getFastSessionServices());
		Assert.notNull(ContextProvider.getLocalResourceSessionFactory(),
				String.format("[%s] is NULL after building process", EntityManagerFactoryImplementor.class));
	}

	private EntityManagerFactoryImplementor build(FastSessionServices fsses) throws IllegalAccessException {
		metadataSources = new MetadataSources(serviceRegistry, true);
		scanPackages();
		// @formatter:off
		MetadataImplementor metadata = MetadataBuildingProcess.build(metadataSources, bootstrapContext, metadataBuildingOptions);
		
		Property prop = metadata.getEntityBindings().stream().filter(ele -> ele.getMappedClass().equals(FileResource.class)).findFirst().orElseThrow().getProperty("createdDate"); 
		
		prop.getValue().setTypeUsingReflection(FileCreationTimeStampType.class.getName(), prop.getName());
		
		SessionFactoryOptionsBuilder optionsBuilder = new SessionFactoryOptionsBuilder(serviceRegistry, bootstrapContext);
		
		optionsBuilder.addSessionFactoryObservers(serviceRegistry.getService(ResultSetMetaDataImplementor.class), ContextProvider.INSTANCE);
		
		return new ManagerFactory(
				localStorage,
				metadata.getTypeConfiguration(),
				metadata,
				serviceRegistry,
				optionsBuilder,
				fsses);
		// @formatter:on
	}

	private void scanPackages() {
		logger.trace("Scanning packages");
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(LocalResource.class));
		scanner.findCandidateComponents(MODEL_PACKAGE).forEach(bean -> {
			try {
				Class<?> clazz = Class.forName(bean.getBeanClassName());

				metadataSources.addAnnotatedClass(clazz);
				metadataSources.addAnnotatedClassName(clazz.getName());
			} catch (ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		});
	}

//	@Autowired
//	private LocalResourceStorage localStorage;
//	private ContextBuildingService contextBuildingService;
//
//	public static final String MODEL_PACKAGE = "adn.service.resource.model.models";
//	private Set<String> identifierGenerators;
//	private Set<ManagerFactoryEventListener> eventListeners = new HashSet<>();
//
//	@Override
//	public void buildAfterStartUp() throws Exception {
//		logger.info(getLoggingPrefix(this) + "Building " + this.getClass());
//		// @formatter:off
//		logger.trace("\n\n"
//				+ "\t\t\t\t\t\t========================================================\n"
//				+ "\t\t\t\t\t\t=          BUILDING LOCAL RESOURCE MANAGEMENT          =\n"
//				+ "\t\t\t\t\t\t========================================================\n");
//		// @formatter:on
//		// init identifierGenerators
//		identifierGenerators = new HashSet<>();
//		// create building service
//		contextBuildingService = ContextBuildingService.createBuildingService();
//		// obtain MutableIdentifierGeneratorFactory from Hibernate service
//		SessionFactoryImplementor sfi = ContextProvider.getApplicationContext().getBean(SessionFactory.class)
//				.unwrap(SessionFactoryImplementor.class);
//		MutableIdentifierGeneratorFactory idGeneratorFactory = sfi.getServiceRegistry()
//				.getService(MutableIdentifierGeneratorFactory.class);
//
//		Assert.notNull(idGeneratorFactory, "Unable to locate IdentifierGeneratorFactory");
//		contextBuildingService.register(MutableIdentifierGeneratorFactory.class, idGeneratorFactory);
//
//		BasicTypeRegistry basicTypeRegistry = sfi.getMetamodel().getTypeConfiguration().getBasicTypeRegistry();
//
//		Assert.notNull(basicTypeRegistry, "Unable to locate BasicTypeRegistry");
//		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(basicTypeRegistry));
//
//		Dialect dialect = sfi.getJdbcServices().getDialect();
//
//		Assert.notNull(basicTypeRegistry, "Unable to locate Dialect");
//		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(dialect));
//
//		// register naming-strategy
//		contextBuildingService.register(NamingStrategy.class, NamingStrategy.DEFAULT_NAMING_STRATEGY);
//		contextBuildingService.register(LocalResourceStorage.class, localStorage);
//		contextBuildingService.register(Metadata.class, new Metadata());
//		Assert.notNull(ResultSetMetaDataImpl.INSTANCE,
//				"Unable to locate instance of " + ResultSetMetaDataImplementor.class);
//		contextBuildingService.register(ResultSetMetaDataImplementor.class, ResultSetMetaDataImpl.INSTANCE);
//
//		FastSessionServices fsses = sfi.getFastSessionServices();
//
//		Assert.notNull(fsses, "Unable to locate instance of " + FastSessionServices.class);
//
//		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(fsses));
//		// @formatter:off
//		prepare();
//		MetadataBuildingContext metadataBuildingContext = new MetadataBuildingContextImpl();
//		
//		contextBuildingService.register(ServiceWrapper.class, new ServiceWrapperImpl<>(metadataBuildingContext));
//		// @formatter:on
//		// inject ResourceManager bean into ApplicationContext
//		// usages of this bean should be obtained via
//		// ContextProvider.getApplicationContext().getBean(ResourceManager.class.getName());
//		// or
//		// ContextProvider.getApplicationContext().getBean([Explicit bean name]);
//		EntityManagerFactoryImplementor sessionFactory = build(sfi.getMetamodel().getTypeConfiguration());
//
//		ContextProvider.getAccess().setLocalResourceSessionFactory(sessionFactory);
//		postBuild();
//		logger.info(getLoggingPrefix(this) + "Finished building " + this.getClass());
//	}
//
//	private void prepare() {
//		importIdentifierGenerator();
//		importDeclaredIdentifierGenerator();
//		importResourceClass();
//	}
//
//	private void importResourceClass() {
//		Metadata metadata = contextBuildingService.getService(Metadata.class);
//		NamingStrategy namingStrategy = contextBuildingService.getService(NamingStrategy.class);
//		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
//
//		scanner.addIncludeFilter(new AnnotationTypeFilter(LocalResource.class));
//		scanner.findCandidateComponents(MODEL_PACKAGE).forEach(bean -> {
//			try {
//				Class<?> clazz = Class.forName(bean.getBeanClassName());
//				LocalResource anno = clazz.getDeclaredAnnotation(LocalResource.class);
//
//				metadata.addImport(StringHelper.hasLength(anno.name()) ? anno.name() : namingStrategy.getName(clazz),
//						clazz);
//			} catch (ClassNotFoundException cnfe) {
//				cnfe.printStackTrace();
//				SpringApplication.exit(ContextProvider.getApplicationContext());
//			}
//		});
//	}
//
//	private void importIdentifierGenerator() {
//		MutableIdentifierGeneratorFactory idGeneratorFactory = contextBuildingService
//				.getService(MutableIdentifierGeneratorFactory.class);
//		String defaultResourceIdentifierName = DefaultResourceIdentifierGenerator.NAME;
//
//		idGeneratorFactory.register(defaultResourceIdentifierName, DefaultResourceIdentifierGenerator.class);
//		identifierGenerators.add(defaultResourceIdentifierName);
//	}
//
//	private void importDeclaredIdentifierGenerator() {
//		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
//		MutableIdentifierGeneratorFactory idGeneratorFactory = contextBuildingService
//				.getService(MutableIdentifierGeneratorFactory.class);
//
//		scanner.addIncludeFilter(new AnnotationTypeFilter(LocalResource.class));
//		scanner.findCandidateComponents(MODEL_PACKAGE).stream().map(bean -> {
//			try {
//				return Class.forName(bean.getBeanClassName());
//			} catch (ClassNotFoundException cnfe) {
//				cnfe.printStackTrace();
//				SpringApplication.exit(ContextProvider.getApplicationContext());
//				return null;
//			}
//		}).forEach(clazz -> {
//			try {
//				for (Field f : clazz.getDeclaredFields()) {
//					if (doesAutoGeneratorPresents(f)) {
//						final GenericGenerator ggAnno = f.getDeclaredAnnotation(GenericGenerator.class);
//
//						if (identifierGenerators.contains(ggAnno.strategy())) {
//							logger.debug("Found " + ggAnno.strategy() + " on " + clazz.getName() + "." + f.getName());
//							continue;
//						}
//
//						Class<?> generatorClass = Class.forName(ggAnno.strategy());
//
//						if (!IdentifierGenerator.class.isAssignableFrom(generatorClass)) {
//							throw new IllegalArgumentException(
//									"Illegal IdentifierGenerator type: " + ggAnno.strategy());
//						}
//
//						idGeneratorFactory.register(ggAnno.strategy(), generatorClass);
//						logger.debug("Registering IdentifierGenerator type: " + generatorClass);
//					}
//				}
//			} catch (ClassNotFoundException cnfe) {
//				cnfe.printStackTrace();
//				SpringApplication.exit(ContextProvider.getApplicationContext());
//			} catch (IllegalArgumentException iae) {
//				iae.printStackTrace();
//				SpringApplication.exit(ContextProvider.getApplicationContext());
//			}
//		});
//	}
//
//	private boolean doesAutoGeneratorPresents(Field f) {
//		GeneratedValue gvAnno = f.getDeclaredAnnotation(GeneratedValue.class);
//		GenericGenerator ggAnno = f.getDeclaredAnnotation(GenericGenerator.class);
//
//		if (gvAnno == null || ggAnno == null) {
//			return false;
//		}
//
//		if (!StringHelper.hasLength(ggAnno.name()) || !StringHelper.hasLength(ggAnno.strategy())
//				|| ggAnno.parameters() == null) {
//			throw new IllegalArgumentException("Invalid GenericGenerator, mandatory fields are empty");
//		}
//
//		return true;
//	}
//
//
//	private void postBuild() {
//		Assert.isTrue(
//				Stream.of(identifierGenerators.toArray(String[]::new))
//						.map(strategy -> contextBuildingService.getService(MutableIdentifierGeneratorFactory.class)
//								.getIdentifierGeneratorClass(strategy) == null)
//						.filter(ele -> ele).findAny().isEmpty(),
//				"Failed to register IdentifierGenerators");
//		eventListeners.forEach(listener -> listener.postBuild(null));
//		logger.trace("Releasing " + MetadataBuildingContext.class);
//		contextBuildingService
//				.getServiceWrapper(MetadataBuildingContext.class, wrapper -> wrapper.orElseThrow().unwrap())
//				.getBootstrapContext().release();
//	}
//
//	private EntityManagerFactoryImplementor build(TypeConfiguration typeConfig)
//			throws IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException {
//		MetadataBuildingContext buildingContext = contextBuildingService
//				.getServiceWrapper(MetadataBuildingContext.class, wrapper -> wrapper.orElseThrow().unwrap());
//
//		return new ManagerFactory(contextBuildingService, typeConfig,
//				buildingContext.getBuildingOptions().getServiceRegistry(),
//				new SessionFactoryOptionsBuilder(buildingContext.getBuildingOptions().getServiceRegistry(),
//						buildingContext.getBootstrapContext()));
//	}
//
//	public static void unsupport() {
//		throw new UnsupportedOperationException("Some implementations might not be supported");
//	}
//
//	@SuppressWarnings("serial")
//	public class ServiceWrapperImpl<T> implements ServiceWrapper<T> {
//
//		private final T instance;
//
//		public ServiceWrapperImpl(T instance) {
//			// TODO Auto-generated constructor stub
//			Assert.notNull(instance, "Cannot wrap a null instance in ServiceWrapper");
//			this.instance = instance;
//		}
//
//		@Override
//		public T unwrap() {
//			return instance;
//		}
//
//	}
//
//	private final BootstrapServiceRegistry bootstrapService = new BootstrapServiceRegistryImpl();
//
//	public class MetadataBuildingContextImpl implements MetadataBuildingContext, Service {
//
//		private final BootstrapContext bootstrapContext = new BootstrapContextImpl();
//		private final InFlightMetadataCollector metadataCollector = new InFlightMetadataCollectorImpl(bootstrapContext,
//				getBuildingOptions());
//
//		@Override
//		public BootstrapContext getBootstrapContext() {
//			return bootstrapContext;
//		}
//
//		@Override
//		public MetadataBuildingOptions getBuildingOptions() {
//			return bootstrapContext.getMetadataBuildingOptions();
//		}
//
//		@Override
//		public MappingDefaults getMappingDefaults() {
//			return bootstrapContext.getMetadataBuildingOptions().getMappingDefaults();
//		}
//
//		@Override
//		public InFlightMetadataCollector getMetadataCollector() {
//			return metadataCollector;
//		}
//
//		@Override
//		public ClassLoaderAccess getClassLoaderAccess() {
//			return bootstrapContext.getClassLoaderAccess();
//		}
//
//		@Override
//		public ObjectNameNormalizer getObjectNameNormalizer() {
//
//			return null;
//		}
//
//		public class BootstrapContextImpl implements BootstrapContext {
//
//			private MetadataBuildingOptionsImpl options = new MetadataBuildingOptionsImpl();
//			private ClassLoaderAccess classLoaderAccess = new ClassLoaderAccessImpl(
//					locateSessionFactory().getServiceRegistry().requireService(ClassLoaderService.class));
//
//			@Override
//			public StandardServiceRegistry getServiceRegistry() {
//				return locateServiceRegistry(StandardServiceRegistry.class);
//			}
//
//			@Override
//			public MutableJpaCompliance getJpaCompliance() {
//				return null;
//			}
//
//			@Override
//			public TypeConfiguration getTypeConfiguration() {
//				return locateSessionFactory().getMetamodel().getTypeConfiguration();
//			}
//
//			@Override
//			public MetadataBuildingOptions getMetadataBuildingOptions() {
//				return options;
//			}
//
//			@Override
//			public boolean isJpaBootstrap() {
//
//				return false;
//			}
//
//			@Override
//			public void markAsJpaBootstrap() {
//
//			}
//
//			@Override
//			public ClassLoader getJpaTempClassLoader() {
//
//				return null;
//			}
//
//			@Override
//			public ClassLoaderAccess getClassLoaderAccess() {
//				return classLoaderAccess;
//			}
//
//			@Override
//			public ClassmateContext getClassmateContext() {
//
//				return null;
//			}
//
//			@Override
//			public ArchiveDescriptorFactory getArchiveDescriptorFactory() {
//
//				return null;
//			}
//
//			@Override
//			public ScanOptions getScanOptions() {
//
//				return null;
//			}
//
//			@Override
//			public ScanEnvironment getScanEnvironment() {
//
//				return null;
//			}
//
//			@Override
//			public Object getScanner() {
//
//				return null;
//			}
//
//			@Override
//			public ReflectionManager getReflectionManager() {
//
//				return null;
//			}
//
//			@Override
//			public IndexView getJandexView() {
//
//				return null;
//			}
//
//			@Override
//			public Map<String, SQLFunction> getSqlFunctions() {
//				return Collections.emptyMap();
//			}
//
//			@Override
//			public Collection<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjectList() {
//				return Collections.emptyList();
//			}
//
//			@Override
//			public Collection<AttributeConverterInfo> getAttributeConverters() {
//
//				return null;
//			}
//
//			@Override
//			public Collection<CacheRegionDefinition> getCacheRegionDefinitions() {
//
//				return null;
//			}
//
//			@Override
//			public void release() {
//				this.options = null;
//				this.classLoaderAccess = null;
//			}
//
//		}
//
//		public class MetadataBuildingOptionsImpl implements MetadataBuildingOptions {
//
//			@SuppressWarnings("serial")
//			private final StandardServiceRegistry serviceRegistry = new StandardServiceRegistryImpl(bootstrapService,
//					Collections.emptyList(),
//					// @formatter:off
//					Arrays.asList(
//						new ProvidedService<>(MutableIdentifierGeneratorFactory.class, contextBuildingService.getService(MutableIdentifierGeneratorFactory.class)),
//						new ProvidedService<>(JdbcServices.class, locateSessionFactory().getJdbcServices()),
//						new ProvidedService<>(JdbcEnvironment.class, locateSessionFactory().getJdbcServices().getJdbcEnvironment()),
//						new ProvidedService<>(ConfigurationService.class, locateSessionFactory().getServiceRegistry().requireService(ConfigurationService.class)),
//						new ProvidedService<>(RegionFactory.class, locateSessionFactory().getServiceRegistry().requireService(RegionFactory.class)),
//						new ProvidedService<>(ProxyFactoryFactory.class, locateSessionFactory().getServiceRegistry().requireService(ProxyFactoryFactory.class)),
//						new ProvidedService<>(PropertyAccessStrategyResolver.class, new PropertyAccessStrategyResolver() {					
//							@Override
//							public PropertyAccessStrategy resolvePropertyAccessStrategy(@SuppressWarnings("rawtypes") Class containerClass, String explicitAccessStrategyName,
//									EntityMode entityMode) {
//								return PropertyAccessStrategyFieldImpl.INSTANCE;
//							}
//						})
//					),
//					// @formatter:on
//					Collections.emptyMap());
//			private final MappingDefaults mappingDefaults = new MetadataBuilderImpl.MappingDefaultsImpl(
//					getServiceRegistry());
//
//			@Override
//			public StandardServiceRegistry getServiceRegistry() {
//				return serviceRegistry;
//			}
//
//			@Override
//			public MappingDefaults getMappingDefaults() {
//				return mappingDefaults;
//			}
//
//			@Override
//			public List<BasicTypeRegistration> getBasicTypeRegistrations() {
//
//				return null;
//			}
//
//			@Override
//			public ReflectionManager getReflectionManager() {
//
//				return null;
//			}
//
//			@Override
//			public IndexView getJandexView() {
//
//				return null;
//			}
//
//			@Override
//			public ScanOptions getScanOptions() {
//
//				return null;
//			}
//
//			@Override
//			public ScanEnvironment getScanEnvironment() {
//
//				return null;
//			}
//
//			@Override
//			public Object getScanner() {
//
//				return null;
//			}
//
//			@Override
//			public ArchiveDescriptorFactory getArchiveDescriptorFactory() {
//
//				return null;
//			}
//
//			@Override
//			public ClassLoader getTempClassLoader() {
//
//				return null;
//			}
//
//			@Override
//			public ImplicitNamingStrategy getImplicitNamingStrategy() {
//
//				return null;
//			}
//
//			@Override
//			public PhysicalNamingStrategy getPhysicalNamingStrategy() {
//				return PhysicalNamingStrategyStandardImpl.INSTANCE;
//			}
//
//			@Override
//			public SharedCacheMode getSharedCacheMode() {
//
//				return null;
//			}
//
//			@Override
//			public AccessType getImplicitCacheAccessType() {
//
//				return null;
//			}
//
//			@Override
//			public MultiTenancyStrategy getMultiTenancyStrategy() {
//
//				return null;
//			}
//
//			@Override
//			public IdGeneratorStrategyInterpreter getIdGenerationTypeInterpreter() {
//
//				return null;
//			}
//
//			@Override
//			public List<CacheRegionDefinition> getCacheRegionDefinitions() {
//
//				return null;
//			}
//
//			@Override
//			public boolean ignoreExplicitDiscriminatorsForJoinedInheritance() {
//
//				return false;
//			}
//
//			@Override
//			public boolean createImplicitDiscriminatorsForJoinedInheritance() {
//
//				return false;
//			}
//
//			@Override
//			public boolean shouldImplicitlyForceDiscriminatorInSelect() {
//
//				return false;
//			}
//
//			@Override
//			public boolean useNationalizedCharacterData() {
//
//				return false;
//			}
//
//			@Override
//			public boolean isSpecjProprietarySyntaxEnabled() {
//
//				return false;
//			}
//
//			@Override
//			public boolean isNoConstraintByDefault() {
//
//				return false;
//			}
//
//			@Override
//			public List<MetadataSourceType> getSourceProcessOrdering() {
//
//				return null;
//			}
//
//			@Override
//			public Map<String, SQLFunction> getSqlFunctions() {
//
//				return Collections.emptyMap();
//			}
//
//			@Override
//			public List<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjectList() {
//				return Collections.emptyList();
//			}
//
//			@Override
//			public List<AttributeConverterInfo> getAttributeConverters() {
//				return Collections.emptyList();
//			}
//
//		}
//
//	}
//
//	@SuppressWarnings("unchecked")
//	private <T extends ServiceRegistry> T locateServiceRegistry(Class<T> type) {
//		ServiceRegistry serviceResgistry = ContextProvider.getApplicationContext().getBean(SessionFactory.class)
//				.unwrap(SessionFactoryImplementor.class).getServiceRegistry();
//
//		if (ServiceRegistry.class.isAssignableFrom(type)) {
//			return (T) serviceResgistry;
//		}
//
//		throw new ClassCastException(String.format("%s could not be casted to %s", ServiceRegistry.class, type));
//	}
//
//	private SessionFactoryImplementor locateSessionFactory() {
//		return ContextProvider.getApplicationContext().getBean(SessionFactory.class)
//				.unwrap(SessionFactoryImplementor.class);
//	}

}
