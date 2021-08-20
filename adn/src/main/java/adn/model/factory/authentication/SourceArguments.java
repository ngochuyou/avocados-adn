/**
 * 
 */
package adn.model.factory.authentication;

import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public interface SourceArguments<T> extends Arguments<T> {

	String[] getColumns();
	
	boolean hasAssociation();

	String[] getAssociationColumns(int index);

	Set<Integer> getAssociationIndicies();
	
}
