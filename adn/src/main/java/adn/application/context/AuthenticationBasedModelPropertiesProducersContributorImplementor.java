/**
 * 
 */
package adn.application.context;

import static adn.service.internal.Role.ADMIN;
import static adn.service.internal.Role.CUSTOMER;
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
		final Role[] personnels = new Role[] { PERSONNEL };
		final Role[] domained = new Role[] { ADMIN, CUSTOMER, PERSONNEL };
		// @formatter:off
		builder
			.type(Account.class)
				.role(allRoles)
					.anyFields().mask()
//					.field("password").mask()
					.field("id").use("username").publish()
					.field("firstName", "lastName", "photo", "role", "gender", "active").publish()
					.field("birthDate").use(Utils::formatLocalDate)
			.type()
				.role(personnels)
					.field("createdDate").use(Utils::formatLocalDate)
					.field("updatedDate").use(Utils::formatLocalDateTime)
					.field("deactivatedDate").use(Utils::formatLocalDate)
			.and()
			.type(Admin.class)
				.role(ADMIN).anyFields().publish()
				.anyRoles().mask()
			.and()
			.type(Customer.class)
				.role(domained)
					.field("email", "phone", "address", "prestigePoint").publish()
			.and()
			.type(Personnel.class)
				.role(personnels)
					.field("createdBy").publish()
				.anyRoles().mask()
		 	.and()
			.type(Factor.class)
				.role(personnels)
					.field("deactivatedDate").use(Utils::formatLocalDateTime)
					.anyFields().publish()
				.anyRoles().mask()
			.and()
			.type(Provider.class)
				.role(personnels).anyFields().publish()
				.anyRoles().mask()
			.and()
			.type(Category.class)
				.role(allRoles).field("id", "name", "active").publish()
			.type()
				.role(personnels).anyFields().publish()
			.and()
			.type(Product.class)
				.role(allRoles)
					.field("id", "name", "price", "category", "images", "description", "rating").publish()
					.anyFields().mask()
			.type()
				.role(personnels)
					.field("updatedTimestamp", "createdTimestamp").use(Utils::formatLocalDateTime)
					.anyFields().publish()
				.anyRoles().mask();
		// @formatter:on
	}

}
