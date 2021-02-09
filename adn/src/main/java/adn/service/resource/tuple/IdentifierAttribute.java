/**
 * 
 */
package adn.service.resource.tuple;

import javax.persistence.metamodel.Attribute;

import adn.service.resource.persister.IdentifierGenerator;

/**
 * @author Ngoc Huy
 *
 */
public interface IdentifierAttribute<X, Y> extends Attribute<X, Y> {

	boolean isVirtual();

	boolean isEmbedded();

	IdentifierValue getUnsavedValue();
	
	IdentifierGenerator getIdentifierGenerator();

	boolean hasIdentifierMapper();

}
