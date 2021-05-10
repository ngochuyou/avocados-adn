/**
 * 
 */
package adn.service.resource.factory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.SharedCacheMode;

import org.hibernate.MultiTenancyStrategy;
import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.boot.AttributeConverterInfo;
import org.hibernate.boot.CacheRegionDefinition;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.spi.ArchiveDescriptorFactory;
import org.hibernate.boot.internal.IdGeneratorInterpreterImpl;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.model.IdGeneratorStrategyInterpreter;
import org.hibernate.boot.model.naming.ImplicitNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.BasicTypeRegistration;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.JpaOrmXmlPersistenceUnitDefaultAware;
import org.hibernate.boot.spi.MappingDefaults;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.MetadataSourceType;
import org.hibernate.cfg.annotations.reflection.JPAMetadataProvider;
import org.hibernate.dialect.function.SQLFunction;
import org.jboss.jandex.IndexView;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;

/**
 * @author Ngoc Huy
 *
 */
public class MetadataBuildingOptionsImpl implements MetadataBuildingOptions, JpaOrmXmlPersistenceUnitDefaultAware {

	private final StandardServiceRegistry serviceRegistry;

	private final MappingDefaults mappingDefaults;
	private final ImplicitNamingStrategy implicitNamingStrategy;
	private ReflectionManager reflectionManager;
	private final IdGeneratorStrategyInterpreter idGeneratorStrategyInterpreter;

	public MetadataBuildingOptionsImpl(StandardServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
		this.mappingDefaults = new MetadataBuilderImpl.MappingDefaultsImpl(this.serviceRegistry);
		this.implicitNamingStrategy = new SpringImplicitNamingStrategy();
		idGeneratorStrategyInterpreter = new IdGeneratorInterpreterImpl();
	}

	public void makeReflectionManager(BootstrapContext boostrapContext) {
		final JavaReflectionManager reflectionManager = new JavaReflectionManager();

		reflectionManager.setMetadataProvider(new JPAMetadataProvider(boostrapContext));
		this.reflectionManager = reflectionManager;
	}

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
		return Collections.emptyList();
//		return Arrays.asList(
//				new BasicTypeRegistration(CreationTimeStampType.INSTANCE,
//						CreationTimeStampType.INSTANCE.getRegistrationKeys()),
//				new BasicTypeRegistration(FileExtensionType.INSTANCE,
//						FileExtensionType.INSTANCE.getRegistrationKeys()));
	}

	@Override
	public ReflectionManager getReflectionManager() {
		return reflectionManager;
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
		return implicitNamingStrategy;
	}

	@Override
	public PhysicalNamingStrategy getPhysicalNamingStrategy() {
		return PhysicalNamingStrategyStandardImpl.INSTANCE;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return SharedCacheMode.ENABLE_SELECTIVE;
	}

	@Override
	public AccessType getImplicitCacheAccessType() {
		return AccessType.TRANSACTIONAL;
	}

	@Override
	public MultiTenancyStrategy getMultiTenancyStrategy() {
		return MultiTenancyStrategy.NONE;
	}

	@Override
	public IdGeneratorStrategyInterpreter getIdGenerationTypeInterpreter() {
		return idGeneratorStrategyInterpreter;
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
		return Arrays.asList(MetadataSourceType.CLASS);
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

	@Override
	public boolean isXmlMappingEnabled() {
		return false;
	}

	@Override
	public void apply(JpaOrmXmlPersistenceUnitDefaults jpaOrmXmlPersistenceUnitDefaults) {
		// TODO Auto-generated method stub
	}

}
