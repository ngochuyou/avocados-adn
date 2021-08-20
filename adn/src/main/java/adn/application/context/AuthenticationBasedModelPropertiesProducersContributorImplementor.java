/**
 * 
 */
package adn.application.context;

import static adn.service.internal.Role.ADMIN;
import static adn.service.internal.Role.CUSTOMER;
import static adn.service.internal.Role.PERSONNEL;

import adn.application.context.builders.DefaultAuthenticationBasedModelPropertiesProducerFactory.AuthenticationBasedModelPropertiesProducersContributor;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Category;
import adn.model.entities.Customer;
import adn.model.entities.Factor;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.Provider;
import adn.model.entities.StockDetail;
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
					.field("id").use("username").publish()
					.field("firstName", "lastName", "photo", "role", "gender", "active").publish()
			.type()
				.role(personnels)
					.field("address", "email", "phone").publish()
					.field("createdDate", "deactivatedDate", "birthDate").use(Utils::ld)
					.field("updatedDate").use(Utils::ldt)
			.and()
			.type(Admin.class)
				.role(ADMIN).anyFields().publish()
				.anyRoles().mask()
			.and()
			.type(Customer.class)
				.role(domained)
					.field("prestigePoint").publish()
			.and()
			.type(Personnel.class)
				.role(personnels)
					.field("createdBy", "department").publish()
				.anyRoles().mask()
		 	.and()
			.type(Factor.class)
				.role(personnels)
					.field("deactivatedDate").use(Utils::ldt)
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
					.field("updatedTimestamp", "createdTimestamp").use(Utils::ldt)
					.anyFields().publish()
				.anyRoles().mask()
			.and()
			.type(StockDetail.class)
				.role(allRoles)
					.field("id", "product", "size", "numericSize", "color", "material", "status", "active", "description").publish()
					.anyFields().mask()
			.type()
				.role(personnels)
					.field("stockedBy", "soldBy", "provider").publish()
					.field("stockedTimestamp", "updatedTimestamp").use(Utils::ldt);
		// @formatter:on
	}

}
