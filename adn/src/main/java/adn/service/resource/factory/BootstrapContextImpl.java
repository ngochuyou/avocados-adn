/**
 * 
 */
package adn.service.resource.factory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.boot.AttributeConverterInfo;
import org.hibernate.boot.CacheRegionDefinition;
import org.hibernate.boot.archive.scan.spi.ScanEnvironment;
import org.hibernate.boot.archive.scan.spi.ScanOptions;
import org.hibernate.boot.archive.spi.ArchiveDescriptorFactory;
import org.hibernate.boot.internal.ClassLoaderAccessImpl;
import org.hibernate.boot.internal.ClassmateContext;
import org.hibernate.boot.model.relational.AuxiliaryDatabaseObject;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.jpa.internal.MutableJpaComplianceImpl;
import org.hibernate.jpa.spi.MutableJpaCompliance;
import org.hibernate.type.spi.TypeConfiguration;
import org.jboss.jandex.IndexView;

/**
 * @author Ngoc Huy
 *
 */
public class BootstrapContextImpl implements BootstrapContext {

	private final StandardServiceRegistry serviceRegistry;
	private final MetadataBuildingOptions metadataBuildingOptions;

	private final ClassLoaderAccess classLoaderAccess;
	private final TypeConfiguration typeConfiguration;
	private final MutableJpaCompliance mutableJpaCompliance;

	public BootstrapContextImpl(StandardServiceRegistry serviceRegistry,
			MetadataBuildingOptions metadataBuildingOptions) {
		this.serviceRegistry = serviceRegistry;
		this.metadataBuildingOptions = metadataBuildingOptions;

		this.typeConfiguration = new TypeConfiguration();
		this.classLoaderAccess = new ClassLoaderAccessImpl(serviceRegistry.getService(ClassLoaderService.class));
		mutableJpaCompliance = new MutableJpaComplianceImpl(
				Map.of(AvailableSettings.JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE, true,
						AvailableSettings.STATIC_METAMODEL_POPULATION, "skipUnsupported"),
				true);
	}

	@Override
	public StandardServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}

	@Override
	public MutableJpaCompliance getJpaCompliance() {
		return mutableJpaCompliance;
	}

	@Override
	public TypeConfiguration getTypeConfiguration() {
		return typeConfiguration;
	}

	@Override
	public MetadataBuildingOptions getMetadataBuildingOptions() {
		return metadataBuildingOptions;
	}

	@Override
	public boolean isJpaBootstrap() {
		return true;
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

	@SuppressWarnings("deprecation")
	@Override
	public ReflectionManager getReflectionManager() {
		return metadataBuildingOptions.getReflectionManager();
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
		return Collections.emptyList();
	}

	@Override
	public Collection<CacheRegionDefinition> getCacheRegionDefinitions() {
		return Collections.emptyList();
	}

	@Override
	public void release() {
		((ClassLoaderAccessImpl) classLoaderAccess).release();
	}

}
