/**
 * 
 */
package adn.application.context;

import static adn.application.context.builders.CredentialFactory.owner;
import static adn.service.DepartmentCredential.CUSTOMER_SERVICE_CREDENTIAL;
import static adn.service.DepartmentCredential.PERSONNEL_CREDENTIAL;
import static adn.service.DepartmentCredential.SALE_CREDENTIAL;
import static adn.service.DepartmentCredential.STOCK_CREDENTIAL;
import static adn.service.internal.Role.ANONYMOUS;
import static adn.service.internal.Role.CUSTOMER;
import static adn.service.internal.Role.HEAD;

import java.util.Map;
import java.util.stream.Stream;

import adn.application.context.builders.CredentialFactory;
import adn.application.context.builders.DynamicMapModelProducerFactoryImpl.ModelProducerFactoryContributor;
import adn.helpers.Utils;
import adn.model.entities.Category;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.ProductCost;
import adn.model.entities.Provider;
import adn.model.entities.User;
import adn.model.entities.id.ProductCostId;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Personnel;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._Provider;
import adn.model.entities.metadata._User;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.ModelProducerFactoryBuilder;
import adn.model.factory.authentication.ModelProducerFactoryBuilder.WithType;

/**
 * @author Ngoc Huy
 *
 */
public class ModelProducerFactoryContributorImplementor implements ModelProducerFactoryContributor {

	@Override
	public void contribute(ModelProducerFactoryBuilder builder) {
		// @formatter:off
		user(builder);
		provider(builder);
		category(builder);
		product(builder);
//			.type(Factor.class)
//				.roles(personnels)
//					.fields("deactivatedDate").use(localDateTimeFormatter)
//					.anyFields().publish()
//				.roles(HEAD).publish()
//			.type(Provider.class)
//				.roles(personnels).departments(sale()).publish()
//				.roles(HEAD).publish()
//			.type(Category.class)
//				.roles(allRoles).fields("id", "name", "active").publish()
//				.roles(personnels).departments(sale()).publish()
//				.roles(HEAD).publish()
//			.type(Product.class)
//				.roles(allRoles)
//					.fields("id", "name", "price", "category", "images", "description", "rating").publish()
//					.anyFields().mask()
//				.roles(personnels).departments(sale())
//					.fields("updatedTimestamp", "createdTimestamp").use(localDateTimeFormatter)
//					.anyFields().publish()
//				.roles(HEAD).publish()
//			.type(StockDetail.class)
//				.roles(allRoles)
//					.fields("id", "product", "size", "numericSize", "color", "material", "status", "active", "description").publish()
//					.anyFields().mask()
//				.roles(personnels).departments(sale())
//					.fields("stockedBy", "soldBy", "provider").publish()
//					.fields("stockedTimestamp", "updatedTimestamp").use(localDateTimeFormatter)
//				.roles(HEAD).publish()
//			.type(Provider.class)
//				.roles(personnels).departments(stock(), sale()).publish()
//				.fields("deactivatedDate").use(localDateTimeFormatter)
//				.roles(HEAD).publish()
//			.type(Department.class)
//				.roles(personnels).departments(personnel()).publish()
//				.roles(HEAD).publish()
//			.type(ProductProviderDetail.class)
//				.roles(personnels).departments(sale()).publish()
//				.fields("appliedTimestamp", "droppedTimestamp").use(localDateTimeFormatter)
//				.roles(HEAD).publish();
		// @formatter:on
	}

	private void user(ModelProducerFactoryBuilder builder) {
		WithType<User> user = builder.type(User.class);
		// @formatter:off
		user
			.credentials(authorized())
				.fields(_User.id).use(_User._id).publish()
				.fields(_User.firstName, _User.lastName, _User.photo,
						_User.role, _User.gender, _User.active,
						_User.email, _User.phone, _User.birthDate,
						_User.locked).publish()
				.anyFields().mask()
			.credentials(PERSONNEL_CREDENTIAL, HEAD)
				.fields(_User.address).publish();

		builder.type(Customer.class)
			.credentials(SALE_CREDENTIAL, CUSTOMER_SERVICE_CREDENTIAL, HEAD, owner())
				.fields(_Customer.prestigePoint).publish();
		
		builder.type(Personnel.class)
			.credentials(HEAD, PERSONNEL_CREDENTIAL, owner())
				.fields(_Personnel.createdBy, _Personnel.department).publish()
			.credentials(personnel())
				.fields(_Personnel.department).publish();
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private void provider(ModelProducerFactoryBuilder builder) {
		WithType<Provider> provider = builder.type(Provider.class);
		// @formatter:off
		provider
			.credentials(SALE_CREDENTIAL, STOCK_CREDENTIAL, HEAD)
				.fields(_Provider.id, _Provider.name, _Provider.active, _Provider.productCosts).publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Provider.email, _Provider.phoneNumbers, _Provider.address,
						_Provider.representatorName, _Provider.website)
				.publish();
		
		WithType<ProductCost> cost = builder.type(ProductCost.class);
		
		cost
			.credentials(SALE_CREDENTIAL, STOCK_CREDENTIAL, HEAD)
				.fields(_ProductCost.cost,
						_ProductCost.productId, _ProductCost.providerId, 
						_ProductCost.product, _ProductCost.provider,
						_ProductCost.createdTimestamp)
				.publish()
				.fields(_ProductCost.id)
					.useFunction((args, credential) -> {
						ProductCostId id = (ProductCostId) args.getSource();
						
						return Map.of(
								_ProductCost.productId, id.getProductId(),
								_ProductCost.providerId, id.getProviderId(),
								_ProductCost.createdTimestamp, Utils.ldt(id.getCreatedTimestamp())
							);
					})
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_ProductCost.droppedTimestamp, _ProductCost.approvedTimestamp,
						_ProductCost.approvedBy)
				.publish();
		// @formatter:on
	}

	private Credential[] from(Credential... credentials) {
		return credentials;
	}

	private Credential[] and(Credential[]... credentialsSets) {
		return Stream.of(credentialsSets).flatMap(credentials -> Stream.of(credentials)).toArray(Credential[]::new);
	}

	private Credential[] personnel() {
		return CredentialFactory.allPersonnelsCredentials().toArray(Credential[]::new);
	}

	private Credential[] any() {
		return and(operator(), from(ANONYMOUS, CUSTOMER));
	}

	private Credential[] operator() {
		return and(personnel(), from(HEAD));
	}

	private Credential[] authorized() {
		return and(operator(), from(owner()));
	}

	private void product(ModelProducerFactoryBuilder builder) {
		WithType<Product> product = builder.type(Product.class);
		// @formatter:off
		product
			.credentials(any())
				.fields(_Product.id, _Product.name, _Product.category,
						_Product.images, _Product.description,
						_Product.rating, _Product.items)
				.publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Product.createdBy, _Product.createdDate,
						_Product.lastModifiedBy, _Product.lastModifiedDate,
						_Product.active, _Product.approvedBy,
						_Product.approvedTimestamp)
				.publish();
		// @formatter:on
	}

	private void category(ModelProducerFactoryBuilder builder) {
		WithType<Category> product = builder.type(Category.class);
		// @formatter:off
		product
			.credentials(any())
				.fields(_Category.id, _Category.description, _Category.name)
				.publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Category.products, _Category.active)
				.publish();
		// @formatter:on
	}

}
