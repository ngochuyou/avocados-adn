/**
 * 
 */
package adn.service.resource.local;

import org.hibernate.tuple.ValueGenerator;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourcePropertyValueGenerator<T> extends ValueGenerator<T> {

	T generateValue(ResourceManagerFactory factory, Object owner);

}
