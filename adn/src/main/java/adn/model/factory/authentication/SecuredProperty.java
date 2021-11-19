/**
 * 
 */
package adn.model.factory.authentication;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.model.DomainEntity;

/**
 * @author Ngoc Huy
 *
 */
public interface SecuredProperty<T extends DomainEntity> {

	Class<T> getOwningType();

	Credential getCredential();

	String getName();

	String getAlias();

	HandledBiFunction<Arguments<?>, Credential, ?, Exception> getFunction();

}
