/**
 * 
 */
package adn.service.resource.persister;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * @author Ngoc Huy
 *
 */
public interface IdentifierGenerator {

	Serializable generate(EntityManager resourceManager, Object o) throws IllegalStateException;

}
