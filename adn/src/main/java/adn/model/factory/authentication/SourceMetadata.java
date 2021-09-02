/**
 * 
 */
package adn.model.factory.authentication;

import java.util.List;
import java.util.Set;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface SourceMetadata<T extends DomainEntity> {

	/**
	 * @return the entity type of this source
	 */
	Class<T> getEntityType();

	/**
	 * Get the representation class of this source If this source is a POJO then
	 * it's the POJO class</br>
	 * If this source is a Collection<Object[]>, returns Object[].class</br>
	 * If this source is a Collection<POJO>, returns POJO class</br>
	 * 
	 * @return the type of this source
	 */
	Class<?> getRepresentation();

	/**
	 * @return the representation of this source
	 */
	SourceType getSourceType();

	/**
	 * Named columns to which this source is mapped
	 * 
	 * @return named columns
	 */
	List<String> getColumns();

	/**
	 * Set the named columns to which this source is mapped
	 */
	void setColumns(List<String> columns);

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
	SourceMetadata<? extends DomainEntity> getAssociationMetadata(int index);

}
