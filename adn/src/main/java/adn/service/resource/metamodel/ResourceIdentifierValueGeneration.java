/**
 * 
 */
package adn.service.resource.metamodel;

import static adn.service.resource.local.ResourceManagerFactory.unsupport;

import java.io.Serializable;

import org.hibernate.tuple.GenerationTiming;

import adn.service.resource.local.AnnotationBasedResourceValueGeneration;
import adn.service.resource.local.ResourceIdentifier;
import adn.service.resource.local.ResourcePropertyValueGenerator;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceIdentifierValueGeneration extends AnnotationBasedResourceValueGeneration {

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