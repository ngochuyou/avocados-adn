/**
 * 
 */
package adn.model.factory.authentication;

import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public interface SourceMetadata {

	/**
	 * Get the representation class of this source
	 * If this source is a POJO then it's the POJO class</br>
	 * If this source is a Collection<Object[]>, returns Object[].class</br>
	 * If this source is a Collection<POJO>, returns POJO class</br>
	 * 
	 * @return the type of this source
	 */
	Class<?> getType();
	
	/**
	 * @return the representation of this source
	 */
	SourceType getSourceType();

	/**
	 * Named columns to which this source is mapped
	 * 
	 * @return named columns
	 */
	String[] getColumns();

	/**
	 * @return whether this source contains any association
	 */
	boolean hasAssociation();

	/**
	 * @return indices at which values are associations
	 */
	Set<Integer> getAssociationIndices();

	/**
	 * @param index index of the association regarding to this source
	 * @return the {@code SourceMetadata} of the association
	 */
	SourceMetadata getAssociationMetadata(int index);
	
}
