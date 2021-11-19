/**
 * 
 */
package adn.model.factory.authentication;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface SingleSource<T extends DomainEntity> extends SourceArguments<T, Object[]> {

	@Override
	Object[] getSource();

}
