/**
 * 
 */
package adn.model.factory.authentication;

import java.util.List;

import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface BatchedSource<E extends DomainEntity> extends SourceArguments<E, List<Object[]>> {

	@Override
	List<Object[]> getSource();

}
