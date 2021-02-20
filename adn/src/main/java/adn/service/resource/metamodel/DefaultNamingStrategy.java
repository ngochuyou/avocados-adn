/**
 * 
 */
package adn.service.resource.metamodel;

import java.io.Serializable;

import javax.persistence.Entity;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class DefaultNamingStrategy implements NamingStrategyDelegate, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String determineResourceName(Object resource) throws Exception {
		// TODO Auto-generated method stub
		Assert.notNull(resource, "Resource cannoy be null");
		// look for @Entity
		Class<?> clazz = resource.getClass();
		Entity anno;

		if ((anno = clazz.getDeclaredAnnotation(Entity.class)) != null) {
			if (Strings.hasLength(anno.name())) {
				return anno.name();
			}
		}
		// use camel cased class name
		return Strings.toCamel(Strings.WHITESPACE_CHARS + clazz.getSimpleName(), Strings.WHITESPACE_CHARS);
	}

}
