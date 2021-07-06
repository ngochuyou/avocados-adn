/**
 * 
 */
package adn.application.context;

import adn.application.context.DefaultAuthenticationBasedModelPropertiesProducerFactory.AuthenticationBasedModelPropertiesProducersContributor;
import adn.model.entities.Account;
import adn.model.factory.property.production.authentication.AuthenticationBasedModelPropertiesProducersBuilder;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class AuthenticationBasedModelPropertiesProducersContributorImplementor
		implements AuthenticationBasedModelPropertiesProducersContributor {

	@Override
	public void contribute(AuthenticationBasedModelPropertiesProducersBuilder builder) {
		final Role[] allRoles = Role.values();
		// @formatter:off
		builder
			.type(Account.class).publish();
		// @formatter:on
	}

}
