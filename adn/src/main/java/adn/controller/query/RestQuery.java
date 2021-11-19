/**
 * 
 */
package adn.controller.query;

import java.util.Set;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface RestQuery<T extends DomainEntity> {

	Class<T> getEntityType();

	/**
	 * @return whether the query is empty
	 */
	boolean hasCriteria();

	boolean hasColumns();

	/**
	 * @return whether any associations are being requested
	 */
	boolean hasAssociation();

	boolean containsColumn(String columnName);

	Set<String> getColumns();

}
