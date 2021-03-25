/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.tuple.AnnotationValueGeneration;

/**
 * @author Ngoc Huy
 *
 */
public interface AnnotationBasedResourceValueGeneration extends AnnotationValueGeneration<ResourceIdentifier> {

	@Override
	ResourcePropertyValueGenerator<Serializable> getValueGenerator();

}
