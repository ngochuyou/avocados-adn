/**
 * 
 */
package adn.service.resource.type;

import javax.persistence.Column;

import org.hibernate.type.BasicType;

/**
 * Columns of this type {@link Column#updatable} with {@code false} value
 * 
 * 
 * @author Ngoc Huy
 *
 */
public interface NoOperationSet extends BasicType {

}
