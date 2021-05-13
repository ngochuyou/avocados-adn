/**
 * 
 */
package adn.service.resource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.config.internal.ConfigurationServiceImpl;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.property.access.internal.PropertyAccessStrategyFieldImpl;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.PropertyAccessStrategyResolver;
import org.hibernate.service.Service;
import org.hibernate.service.internal.ProvidedService;
import org.hibernate.service.internal.SessionFactoryServiceRegistryImpl;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistryFactory;
import org.hibernate.tool.schema.Action;
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
	// @formatter:off
	private static final List<Class<? extends Service>> STANDARD_SERVICES_CLASSES = Collections.unmodifiableList(Arrays.asList(
			MutableIdentifierGeneratorFactory.class,
			JdbcServices.class,
			JdbcEnvironment.class,
			RegionFactory.class,
			ConfigurationService.class,
			ProxyFactoryFactory.class,
			CfgXmlAccessService.class,
			CacheImplementor.class,
			PersisterFactory.class,
			SessionFactoryServiceRegistryFactory.class,
			ResultSetMetaDataImplementor.class,
			PropertyAccessStrategyResolver.class
	));
	private final BiFunction<SessionFactoryImplementor, Class<? extends Service>, ? extends Service> defaultServiceGetter = new BiFunction<>() {

		@Override
		public Service apply(SessionFactoryImplementor t, Class<? extends Service> type) {
			return t.getServiceRegistry().getService(type);
		}
		
	};
	private final Map<Class<? extends Service>, Object> serviceGetters = Collections.unmodifiableMap(Map.of(
			JdbcServices.class, new Function<SessionFactoryImplementor, JdbcServices>() {
				
				@Override
				public JdbcServices apply(SessionFactoryImplementor sfi) {
					return sfi.getJdbcServices();
				}
				
			},
			JdbcEnvironment.class, new Function<SessionFactoryImplementor, JdbcEnvironment>() {
				
				@Override
				public JdbcEnvironment apply(SessionFactoryImplementor sfi) {
					return sfi.getJdbcServices().getJdbcEnvironment();
				}
				
			},
			ConfigurationService.class, new Function<SessionFactoryImplementor, ConfigurationService>() {

				@Override
				public ConfigurationService apply(SessionFactoryImplementor sfi) {
					return disableHbm2DdlAuto(sfi.getServiceRegistry().getService(ConfigurationService.class));
				}
				
			},
			SessionFactoryServiceRegistryFactory.class, new Function<SessionFactoryImplementor, SessionFactoryServiceRegistryFactory>() {

				@Override
				public SessionFactoryServiceRegistryFactory apply(SessionFactoryImplementor sfi) {
					return new SessionFactoryServiceRegistryFactoryImpl(sfi);
				}
				
			},
			PropertyAccessStrategyResolver.class, new Supplier<PropertyAccessStrategyResolver>() {

				@SuppressWarnings("serial")
				@Override
				public PropertyAccessStrategyResolver get() {
					return new PropertyAccessStrategyResolver() {
						@Override
						public PropertyAccessStrategy resolvePropertyAccessStrategy(@SuppressWarnings("rawtypes") Class containerClass, String explicitAccessStrategyName,
								EntityMode entityMode) {
							return PropertyAccessStrategyFieldImpl.INSTANCE;
						}
					};
				}
				
			},
			ResultSetMetaDataImplementor.class, new Supplier<ResultSetMetaDataImplementor>() {

				@Override
				public ResultSetMetaDataImplementor get() {
					return ResultSetMetaDataImpl.INSTANCE;
				}
				
			}
	));
	// @formatter:on
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

		registerCustomIdentifierGeneratorFactory(
				sfi.getServiceRegistry().requireService(MutableIdentifierGeneratorFactory.class));

		serviceRegistry = createStandardServiceRegistry(sfi, bootstrapServiceRegistry);
		metadataBuildingOptions = new MetadataBuildingOptionsImpl(serviceRegistry);
		bootstrapContext = new BootstrapContextImpl(serviceRegistry, metadataBuildingOptions);
		((MetadataBuildingOptionsImpl) metadataBuildingOptions).makeReflectionManager(bootstrapContext);

		assertSessionFactoryAndInject(build(sfi, sfi.getFastSessionServices()));
	}

	private void assertSessionFactoryAndInject(SessionFactory sf) throws IllegalAccessException {
		Assert.notNull(sf, String.format("[%s] is NULL after building process", EntityManagerFactoryImplementor.class));
		ContextProvider.getAccess().setLocalResourceSessionFactory(sf.unwrap(SessionFactoryImpl.class));
		ContextProvider.closeAccess();
	}

	private MutableIdentifierGeneratorFactory registerCustomIdentifierGeneratorFactory(
			MutableIdentifierGeneratorFactory migf) {
		migf.register(DefaultResourceIdentifierGenerator.NAME, DefaultResourceIdentifierGenerator.class);

		return migf;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<ProvidedService> getProvidedServices(SessionFactoryImplementor sfi) {
		return STANDARD_SERVICES_CLASSES.stream().map(role -> {
			if (!serviceGetters.containsKey(role)) {
				return new ProvidedService(role, defaultServiceGetter.apply(sfi, role));
			}

			Object getter = serviceGetters.get(role);

			if (getter instanceof Function) {
				return new ProvidedService(role, ((Function<SessionFactoryImplementor, ?>) getter).apply(sfi));
			}

			if (getter instanceof Supplier) {
				return new ProvidedService(role, ((Supplier<Service>) getter).get());
			}

			logger.error(String.format("Unable to locate service of type [%s]", role.asSubclass(null)));
			SpringApplication.exit(ContextProvider.getApplicationContext());
			return null;
		}).collect(Collectors.toList());
	}

	private StandardServiceRegistry createStandardServiceRegistry(SessionFactoryImplementor sfi,
			BootstrapServiceRegistry bootstrapServiceRegistry, ProvidedService<?>... additional) {
		// @formatter:off
		serviceRegistry = new StandardServiceRegistryImpl(
				bootstrapServiceRegistry,
				Collections.emptyList(),
				getProvidedServices(sfi),
				Collections.emptyMap());
		// @formatter:on
		return serviceRegistry;
	}

	@SuppressWarnings("serial")
	public class SessionFactoryServiceRegistryFactoryImpl implements SessionFactoryServiceRegistryFactory {

		private final SessionFactoryImplementor sfi;

		public SessionFactoryServiceRegistryFactoryImpl(SessionFactoryImplementor sfi) {
			Assert.notNull(sfi, String.format("[%s] must not be null", SessionFactoryImplementor.class));
			this.sfi = sfi;
		}

		@Override
		public SessionFactoryServiceRegistry buildServiceRegistry(SessionFactoryImplementor sessionFactory,
				SessionFactoryOptions sessionFactoryOptions) {
			// @formatter:off
			SessionFactoryServiceRegistryImpl delegatedService = new SessionFactoryServiceRegistryImpl(
					sfi.getServiceRegistry(), // we want to use the service registry from the original SessionFactoryImpl as the parent
					Collections.emptyList(), // we have to initiate all the additional Services before we build our SessionFactory
					getProvidedServices(sfi), // this SessionFactory must be the original one from Hibernate so that we can collect all the configurations
					sessionFactory, // this SessionFactory is the one that we are trying to build
					sessionFactoryOptions);
			// @formatter:on
			return delegatedService;
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ConfigurationService disableHbm2DdlAuto(ConfigurationService cfgService) {
		final Map settings = new HashMap<>();
		// @formatter:off
		final Map<String, Consumer<Map>> disablingFuntions = Map.of(
				AvailableSettings.HBM2DDL_AUTO, (settingMap) -> {
					logger.trace(String.format("Disabling setting [%s] -> [%s]", AvailableSettings.HBM2DDL_AUTO, Action.NONE));
					settings.put(AvailableSettings.HBM2DDL_AUTO, Action.NONE);
				},
				AvailableSettings.HBM2DDL_SCRIPTS_ACTION, (settingMap) -> {
					logger.trace(String.format("Disabling setting [%s] -> [%s]", AvailableSettings.HBM2DDL_SCRIPTS_ACTION, Action.NONE));
					settings.put(AvailableSettings.HBM2DDL_SCRIPTS_ACTION, Action.NONE);
				},
				AvailableSettings.HBM2DDL_DATABASE_ACTION, (settingMap) -> {
					logger.trace(String.format("Disabling setting [%s] -> [%s]", AvailableSettings.HBM2DDL_DATABASE_ACTION, Action.NONE));
					settings.put(AvailableSettings.HBM2DDL_DATABASE_ACTION, Action.NONE);
				}
		);
		final Map<String, Consumer<Map>> injectDefaultFunctions = Map.of(
				AvailableSettings.HBM2DDL_SCRIPTS_ACTION, disablingFuntions.get(AvailableSettings.HBM2DDL_SCRIPTS_ACTION),
				AvailableSettings.HBM2DDL_DATABASE_ACTION, disablingFuntions.get(AvailableSettings.HBM2DDL_DATABASE_ACTION)
		);
		// @formatter:on
		cfgService.getSettings().entrySet().stream().forEach(e -> {
			Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) e;

			if (disablingFuntions.containsKey(entry.getKey())) {
				disablingFuntions.get(entry.getKey()).accept(settings);
				return;
			}

			settings.put(entry.getKey(), entry.getValue());
			return;
		});
		injectDefaultFunctions.entrySet().stream().forEach(entry -> {
			if (settings.containsKey(entry.getKey())) {
				return;
			}

			logger.trace(String.format("Injecting default setting [%s]", entry.getKey()));
			entry.getValue().accept(settings);
		});

		return new ConfigurationServiceImpl(settings);
	}

	private EntityManagerFactoryImplementor build(SessionFactoryImplementor hibernateSessionFactoryInstance,
			FastSessionServices fsses) throws IllegalAccessException {
		metadataSources = new MetadataSources(serviceRegistry, true);
		scanPackages();

		MetadataImplementor metadata = MetadataBuildingProcess.build(metadataSources, bootstrapContext,
				metadataBuildingOptions);
		SessionFactoryOptionsBuilder optionsBuilder = new SessionFactoryOptionsBuilder(serviceRegistry,
				bootstrapContext);

		addSessionFactoryObservers(optionsBuilder);
		// @formatter:off
		EntityManagerFactoryImplementor sf = new ManagerFactory(
				localStorage,
				metadata,
				serviceRegistry,
				optionsBuilder,
				fsses);
		// @formatter:on
		return sf;
	}

	private void addSessionFactoryObservers(SessionFactoryOptionsBuilder optionsBuilder) {
		optionsBuilder.addSessionFactoryObservers(ResultSetMetaDataImpl.INSTANCE);
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

}