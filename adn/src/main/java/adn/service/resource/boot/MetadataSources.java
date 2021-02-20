/**
 * 
 */
package adn.service.resource.boot;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class MetadataSources implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Set<Class<?>> annotatedClasses;
	private Set<Class<?>> annotatedClassnames;

	public MetadataSources() {
		// TODO Auto-generated constructor stub
	}

	public Set<Class<?>> getAnnotatedClasses() {
		return annotatedClasses == null ? Collections.emptySet() : annotatedClasses;
	}

	public Set<Class<?>> getAnnotatedClassnames() {
		return annotatedClassnames == null ? Collections.emptySet() : annotatedClassnames;
	}

}
