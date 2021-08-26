/**
 * 
 */
package adn.application.context;

import static adn.application.context.builders.DepartmentScopeContext.personnel;
import static adn.application.context.builders.DepartmentScopeContext.sale;
import static adn.application.context.builders.DepartmentScopeContext.stock;
import static adn.service.internal.Role.HEAD;
import static adn.service.internal.Role.CUSTOMER;
import static adn.service.internal.Role.PERSONNEL;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;

import adn.application.context.builders.DynamicMapModelProducerFactoryImpl.ModelProducerFactoryContributor;
import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.Utils;
import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Category;
import adn.model.entities.Customer;
import adn.model.entities.Department;
import adn.model.entities.Factor;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.Provider;
import adn.model.entities.StockDetail;
import adn.model.factory.authentication.Arguments;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.ModelProducerFactoryBuilder;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class ModelProducerFactoryContributorImplementor implements ModelProducerFactoryContributor {

	public static final HandledBiFunction<Arguments<?>, Credential, ?, Exception> LOCAL_DATE_FORMATTER = new HandledBiFunction<>() {
		@Override
		public Object apply(Arguments<?> arg, Credential credential) {
			Object source = arg.getSource();

			return source != null ? Utils.localDate((LocalDate) source) : null;
		}
	};
	public static final HandledBiFunction<Arguments<?>, Credential, ?, Exception> LOCAL_DATE_TIME_FORMATTER = new HandledBiFunction<>() {
		@Override
		public Object apply(Arguments<?> arg, Credential credential) {
			return Utils.localDateTime((LocalDateTime) arg.getSource());
		}
	};

	@SuppressWarnings("unchecked")
	@Override
	public void contribute(ModelProducerFactoryBuilder builder) {
		final Role[] allRoles = Role.values();
		final Role[] personnels = new Role[] { PERSONNEL };
		final Role[] domained = new Role[] { HEAD, CUSTOMER, PERSONNEL };
		HandledBiFunction<Arguments<?>, Credential, ?, Exception>[] localDateFormatter = (HandledBiFunction<Arguments<?>, Credential, ?, Exception>[]) Array
				.newInstance(HandledBiFunction.class, 1);

		localDateFormatter[0] = LOCAL_DATE_FORMATTER;

		HandledBiFunction<Arguments<?>, Credential, ?, Exception>[] localDateTimeFormatter = (HandledBiFunction<Arguments<?>, Credential, ?, Exception>[]) Array
				.newInstance(HandledBiFunction.class, 1);

		localDateTimeFormatter[0] = LOCAL_DATE_TIME_FORMATTER;

		// @formatter:off
		builder
			.type(Account.class)
				.roles(allRoles)
					.fields("id").use("username").publish()
					.fields("firstName", "lastName", "photo", "role", "gender", "active").publish()
					.anyFields().mask()
				.roles(personnels)
					.fields("address", "email", "phone").publish()
					.fields("createdDate", "deactivatedDate", "birthDate")
						.use(localDateFormatter)
					.fields("updatedDate")
						.use(localDateTimeFormatter)
			.type(Admin.class)
				.roles(HEAD).publish()
			.type(Customer.class)
				.roles(domained)
					.fields("prestigePoint").publish()
					.anyFields().mask()
			.type(Personnel.class)
				.roles(personnels)
					.fields("createdBy", "department").publish()
			.type(Factor.class)
				.roles(personnels)
					.fields("deactivatedDate").use(localDateTimeFormatter)
					.anyFields().publish()
			.type(Provider.class)
				.roles(personnels).departments(sale()).publish()
			.type(Category.class)
				.roles(allRoles).fields("id", "name", "active").publish()
				.roles(personnels).departments(sale()).publish()
			.type(Product.class)
				.roles(allRoles)
					.fields("id", "name", "price", "category", "images", "description", "rating").publish()
					.anyFields().mask()
				.roles(personnels).departments(sale())
					.fields("updatedTimestamp", "createdTimestamp").use(localDateTimeFormatter)
					.anyFields().publish()
			.type(StockDetail.class)
				.roles(allRoles)
					.fields("id", "product", "size", "numericSize", "color", "material", "status", "active", "description").publish()
					.anyFields().mask()
				.roles(personnels).departments(sale())
					.fields("stockedBy", "soldBy", "provider").publish()
					.fields("stockedTimestamp", "updatedTimestamp").use(localDateTimeFormatter)
			.type(Provider.class)
				.roles(personnels).departments(stock(), sale()).publish()
				.fields("deactivatedDate").use(localDateTimeFormatter)
			.type(Department.class)
				.roles(personnels).departments(personnel()).publish()
			.type(ProductProviderDetail.class)
				.roles(personnels).departments(sale()).publish()
				.fields("appliedTimestamp", "droppedTimestamp").use(localDateTimeFormatter);
		// @formatter:on
	}

}
