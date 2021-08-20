/**
 * 
 */
package adn.model.factory.authentication;

/**
 * @author Ngoc Huy
 *
 */
public interface SingleSource extends SourceArguments<Object[]> {

	@Override
	Object[] getSource();

}
