/**
 * 
 */
package adn.application.context;

import adn.application.context.DefaultAuthenticationBasedModelPropertiesProducerFactory.AuthenticationBasedModelPropertiesProducersContributor;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Factor;
import adn.model.entities.Personnel;
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
		final Role[] personnels = new Role[] { Role.ADMIN, Role.PERSONNEL, Role.MANAGER, Role.EMPLOYEE };
		// @formatter:off
		builder
			.type(Account.class)
				.role(allRoles)
					.field("password").mask()
					.field("id").use("username").publish()
					.fields("firstName", "lastName", "photo", "role", "gender", "active").publish()
					.field("birthDay").use(Utils::localDateToDate)
					.anyFields().mask()
					.type()
				.role(personnels)
					.field("createdDate").use(Utils::localDateToDate)
					.field("updatedDate").use(Utils::localDateTimeToDate)
					.anyFields().mask()
					.type()
				.role(Role.ADMIN)
					.field("email", "phone").publish()
					.type()
					.anyRoles().mask()
					.and()
			.type(Admin.class)
				.anyRoles().mask()
				.and()
			.type(Customer.class)
				.role(allRoles)
					.field("email", "phone", "address", "prestigePoint").publish()
					.anyFields().mask()
			.and()
				.type(Personnel.class)
					.role(personnels)
						.field("createdBy").publish()
					.anyRoles().mask()
				.type()
					.anyRoles().anyFields().mask()
		 	.and()
				.type(Factor.class)
					.role(Role.ADMIN, Role.PERSONNEL).publish()
					.anyRoles().mask()
			.and()
				.anyType().role(Role.ADMIN).publish();
		// @formatter:on
	}

}
