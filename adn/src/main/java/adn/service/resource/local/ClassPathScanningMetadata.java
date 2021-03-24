/**
 * 
 */
package adn.service.resource.local;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import adn.application.Constants;

/**
 * @author Ngoc Huy
 *
 */
public class ClassPathScanningMetadata implements Metadata {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Set<Class<?>> modelClassSet = new HashSet<>();

	/**
	 * 
	 */
	public ClassPathScanningMetadata() {
		// TODO Auto-generated constructor stub
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		modelClassSet = scanner.findCandidateComponents(Constants.resourceModelPackage)
			.stream()
			.map(bean -> {
				try {
					logger.debug("Found new resource Model of type: " + bean.getBeanClassName());
	
					return Class.forName(bean.getBeanClassName());
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					logger.error("Failed to obtain model type: " + bean.getBeanClassName());
					return null;
				}
			}).collect(Collectors.toSet());
	}

	public Set<Class<?>> getModelClassSet() {
		return modelClassSet;
	}

	public void setModelClassSet(Set<Class<?>> modelClassSet) {
		this.modelClassSet = modelClassSet;
	}

}
