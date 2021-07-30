/**
 * 
 */
package adn.application.context;

import static adn.service.internal.Role.ADMIN;
import static adn.service.internal.Role.EMPLOYEE;
import static adn.service.internal.Role.MANAGER;
import static adn.service.internal.Role.PERSONNEL;

import adn.application.context.DefaultAuthenticationBasedModelPropertiesProducerFactory.AuthenticationBasedModelPropertiesProducersContributor;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Category;
import adn.model.entities.Customer;
import adn.model.entities.Factor;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.Provider;
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
		final Role[] personnels = new Role[] { ADMIN, PERSONNEL, MANAGER, EMPLOYEE };
		// @formatter:off
		builder
			.type(Account.class)
				.role(allRoles)
					.field("password").mask()
					.field("id").use("username").publish()
					.fields("firstName", "lastName", "photo", "role", "gender", "active").publish()
					.field("birthDay").use(Utils::formatLocalDate)
					.anyFields().mask()
					.type()
				.role(personnels)
					.field("createdDate").use(Utils::formatLocalDate)
					.field("updatedDate").use(Utils::formatLocalDateTime)
					.anyFields().mask()
					.type()
				.role(ADMIN)
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
					.role(ADMIN, PERSONNEL).publish()
					.anyRoles().mask()
			.and()
				.type(Provider.class)
					.role(PERSONNEL).publish()
			.and()
				.type(Category.class)
					.role(allRoles).publish()
					.field("deactivatedDate").use(Utils::formatLocalDateTime)
			.and()
				.type(Product.class)
					.role(ADMIN, PERSONNEL)
						.field("createdTimestamp").use(Utils::formatLocalDateTime)
						.field("updatedTimestamp").use(Utils::formatLocalDateTime)
						.anyFields().publish()
					.anyRoles().mask()
			.and()
				.anyType().role(PERSONNEL, ADMIN).publish();
		// @formatter:on
	}

}
