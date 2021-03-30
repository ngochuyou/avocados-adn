/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.tuple.AnnotationValueGeneration;
import org.hibernate.tuple.GenerationTiming;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AnnotationBasedResourceValueGeneration implements AnnotationValueGeneration<ResourceIdentifier> {

	private static final long serialVersionUID = 1L;

	protected final GenerationTiming timing;

	public AnnotationBasedResourceValueGeneration(GenerationTiming timing) {
		// TODO Auto-generated constructor stub
		this.timing = timing;
	}

	@Override
	public abstract ResourcePropertyValueGenerator<Serializable> getValueGenerator();

	public GenerationTiming getTiming() {
		return timing;
	}

}
