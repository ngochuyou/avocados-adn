/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

/**
 * @author Ngoc Huy
 *
 */
public interface ResourceTuplizer<T> {

	Class<T> getType();

	Serializable getId(T owner);

	Getter getIdGetter();

	Setter getIdSetter();

}
