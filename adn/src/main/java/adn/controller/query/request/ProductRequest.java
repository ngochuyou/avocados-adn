/**
 * 
 */
package adn.controller.query.request;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Ngoc Huy
 *
 */
public class ProductRequest extends AbstractColumnRequest {

	@Override
	protected List<List<String>> getAssociationColumns() {
		return new ArrayList<>();
	}
	
	@Override
	public Collection<String> join() {
		return new ArrayList<>(getColumns());
	}

	@Override
	public boolean isEmpty() {
		return getColumns().isEmpty();
	}

	@Override
	public boolean hasAssociation() {
		return false;
	}
	
}
