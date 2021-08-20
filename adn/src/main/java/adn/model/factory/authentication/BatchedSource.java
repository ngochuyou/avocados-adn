/**
 * 
 */
package adn.model.factory.authentication;

import java.util.List;

/**
 * @author Ngoc Huy
 *
 */
public interface BatchedSource extends SourceArguments<List<Object[]>> {

	@Override
	List<Object[]> getSource();

}
