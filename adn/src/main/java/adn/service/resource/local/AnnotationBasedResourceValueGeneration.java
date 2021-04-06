/**
 * 
 */
package adn.service.resource.local;

import javax.persistence.GeneratedValue;

import org.hibernate.tuple.AnnotationValueGeneration;
import org.hibernate.tuple.GenerationTiming;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AnnotationBasedResourceValueGeneration implements AnnotationValueGeneration<GeneratedValue> {

	private static final long serialVersionUID = 1L;

	protected final GenerationTiming timing;

	public AnnotationBasedResourceValueGeneration(GenerationTiming timing) {
		// TODO Auto-generated constructor stub
		this.timing = timing;
	}

	public GenerationTiming getTiming() {
		return timing;
	}

}
