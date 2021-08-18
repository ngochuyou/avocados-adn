/**
 * 
 */
package adn.model.factory.authentication;

import java.util.function.BiFunction;

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
	
	BiFunction<Object, Credential, Object> getFunction();
	
}
