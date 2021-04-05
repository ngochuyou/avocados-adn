/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.Session;
import org.hibernate.tuple.GenerationTiming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.local.AnnotationBasedResourceValueGeneration;
import adn.service.resource.local.ContextBuildingService;
import adn.service.resource.local.Metadata;
import adn.service.resource.local.NamingStrategy;
import adn.service.resource.local.ResourceDescriptor;
import adn.service.resource.local.ResourceIdentifier;
import adn.service.resource.local.ResourceManagerFactory;
import adn.service.resource.local.ResourcePropertyValueGenerator;
import adn.service.resource.models.NamedResource;
import adn.utilities.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class MetamodelImpl implements Metamodel {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ResourceManagerFactory managerFactory;

	private final Map<String, ResourceDescriptor<?>> descriptorsByName = new HashMap<>();

	private final Set<String> managedModels;

//	private static final Map<GenerationTiming, ResourceIdentifierValueGeneration> valueGenerationMap = new HashMap<>();

	private final Map<String, EntityType<?>> entitiesByName;

	/**
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 * 
	 */
	public MetamodelImpl(ContextBuildingService serviceRegistry, ResourceManagerFactory resourceManagerFactory)
			throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		// TODO Auto-generated constructor stub
		Assert.notNull(resourceManagerFactory, "ResourceManagerFactory must not be null");
		this.managerFactory = resourceManagerFactory;

		Metadata metadata = serviceRegistry.getService(Metadata.class);
		NamingStrategy resourceNamingStrategy = serviceRegistry.getService(NamingStrategy.class);

		Assert.notNull(metadata, "Metadata must not be null");

		Set<Class<?>> modelClassSet = metadata.getModelClassSet();

		managedModels = modelClassSet.stream().map(clazz -> resourceNamingStrategy.getName(clazz))
				.collect(Collectors.toSet());
		entitiesByName = new HashMap<>(managedModels.size());
	}

	@Override
	public <X> EntityTypeImpl<X> entity(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(managerFactory.getContextBuildingService().getService(NamingStrategy.class).getName(cls));
	}

	@SuppressWarnings("unchecked")
	public <X> EntityTypeImpl<X> entity(String name) {
		// TODO Auto-generated method stub
		return (EntityTypeImpl<X>) entitiesByName.get(name);
	}

	@Override
	public <X> ManagedType<X> managedType(Class<X> cls) {
		// TODO Auto-generated method stub
		return entity(cls);
	}

	@Override
	public <X> EmbeddableType<X> embeddable(Class<X> cls) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ManagedType<?>> getManagedTypes() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(entitiesByName.values()));
	}

	@Override
	public Set<EntityType<?>> getEntities() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(entitiesByName.values()));
	}

	@Override
	public Set<EmbeddableType<?>> getEmbeddables() {
		// TODO Auto-generated method stub
		return Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	public <T> ResourceDescriptor<T> getResourceDescriptor(String resourceName) {
		// TODO Auto-generated method stub
		if (!managedModels.contains(resourceName)) {
			return null;
		}

		return (ResourceDescriptor<T>) descriptorsByName.get(resourceName);
	}

	public Set<ResourceDescriptor<?>> getResourceDescriptors() {
		// TODO Auto-generated method stub
		return Collections.unmodifiableSet(new HashSet<>(descriptorsByName.values()));
	}

	public ResourceManagerFactory getManagerFactory() {
		return managerFactory;
	}

	public static class ResourceIdentifierValueGenerator implements ResourcePropertyValueGenerator<Serializable> {

		public static final ResourceIdentifierValueGenerator INSTANCE = new ResourceIdentifierValueGenerator();

		public static final String IDENTIFIER_PARTS_SEPERATOR = "_";

		@Override
		@Deprecated
		public Serializable generateValue(Session session, Object owner) {
			// TODO Auto-generated method stub
			unsupport();
			return null;
		}

		@Override
		public Serializable generateValue(ResourceManagerFactory factory, Object object) {
			// TODO Auto-generated method stub
			if (object instanceof NamedResource) {
				// @formatter:off
				NamedResource instance = (NamedResource) object;

				return new StringBuilder(instance.getDirectoryPath())
						.append(new Date().getTime())
						.append(IDENTIFIER_PARTS_SEPERATOR)
						.append(StringHelper.hash(instance.getName()))
						.append(instance.getExtension())
						.toString();
				// @formatter:on
			}

			return String.valueOf(new Date().getTime());
		}

	}

	public static class ResourceIdentifierValueGeneration extends AnnotationBasedResourceValueGeneration {

		private static final long serialVersionUID = 1L;

		private final ResourcePropertyValueGenerator<Serializable> generator = ResourceIdentifierValueGenerator.INSTANCE;

		/**
		 * @param timing
		 */
		public ResourceIdentifierValueGeneration(GenerationTiming timing) {
			super(timing);
			// TODO Auto-generated constructor stub
		}

		public ResourceIdentifierValueGeneration(ResourceIdentifierValueGeneration other) {
			super(GenerationTiming.valueOf(other.timing.toString()));
		}

		@Override
		public GenerationTiming getGenerationTiming() {
			// TODO Auto-generated method stub
			return timing;
		}

		@Override
		public ResourcePropertyValueGenerator<Serializable> getValueGenerator() {
			// TODO Auto-generated method stub
			return generator;
		}

		@Override
		public boolean referenceColumnInSql() {
			// TODO Auto-generated method stub
			unsupport();
			return false;
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {
			// TODO Auto-generated method stub
			unsupport();
			return null;
		}

		@Override
		public void initialize(ResourceIdentifier annotation, Class<?> propertyType) {
			// TODO Auto-generated method stub
		}

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		for (ResourceDescriptor<?> descriptor : descriptorsByName.values()) {
			// @formatter:off
			logger.debug(String.format("\nCreated descriptor for type: %s with name: %s\n"
					+ "\t-idGetter: %s returns %s\n"
					+ "\t-idSetter: %s returns void\n"
					+ "\t-isIdentifierAutoGenerated: %s\n"
					+ "\t-identifierValueGenerator: %s\n"
					+ "\t-generationTiming: %s",
					descriptor.getType(), descriptor.getResourceName(),
					descriptor.getIdentifierGetter().getMethodName(), descriptor.getIdentifierGetter().getReturnType(),
					descriptor.getIdentifierSetter().getMethodName(),
					Boolean.valueOf(descriptor.isIdentifierAutoGenerated()),
					(descriptor.isIdentifierAutoGenerated() ? 
							descriptor.getIdentifierValueGeneration().getValueGenerator().getClass().getName() :
							"NONE"),
					(descriptor.isIdentifierAutoGenerated() ? 
							descriptor.getIdentifierValueGeneration().getGenerationTiming() :
							"NEVER")));
			logger.trace(descriptor.toString());
		}
		// @formatter:on
		return super.toString();
	}

}
