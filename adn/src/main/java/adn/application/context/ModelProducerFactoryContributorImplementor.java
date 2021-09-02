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

import java.util.stream.Stream;

import adn.application.context.builders.CredentialFactory;
import adn.application.context.builders.DynamicMapModelProducerFactoryImpl.ModelProducerFactoryContributor;
import adn.model.entities.Account;
import adn.model.entities.Category;
import adn.model.entities.Customer;
import adn.model.entities.Head;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.ProductProviderDetail;
import adn.model.entities.Provider;
import adn.model.entities.metadata._Account;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._Personnel;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductProviderDetail;
import adn.model.entities.metadata._Provider;
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
		account(builder);
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

	private Credential[] allPersonnelsAnd(Credential... others) {
		return Stream.concat(CredentialFactory.allPersonnelsCredentials().stream(), Stream.of(others))
				.toArray(Credential[]::new);
	}

	private void account(ModelProducerFactoryBuilder builder) {
		WithType<Account> account = builder.type(Account.class);
		// @formatter:off
		account
			.credentials(PERSONNEL_CREDENTIAL, HEAD, owner())
				.fields(_Account.id).use("username").publish()
				.fields(_Account.firstName, _Account.lastName, _Account.photo, _Account.role, _Account.gender, _Account.active,
						_Account.email, _Account.phone, _Account.birthDate).publish()
				.anyFields().mask()
			.credentials(owner())
				.fields(_Account.createdDate).publish()
			.credentials(PERSONNEL_CREDENTIAL, HEAD)
				.fields(_Account.address, _Account.createdDate, _Account.deactivatedDate, _Account.updatedDate).publish();
		
		builder.type(Head.class).roles(HEAD).publish();
		
		builder.type(Customer.class)
			.credentials(SALE_CREDENTIAL, CUSTOMER_SERVICE_CREDENTIAL, HEAD, owner())
				.fields(_Customer.prestigePoint).publish();
		
		builder.type(Personnel.class)
			.credentials(HEAD, PERSONNEL_CREDENTIAL, owner())
				.fields(_Personnel.createdBy, _Personnel.department).publish()
			.credentials(allPersonnelsAnd())
				.fields(_Personnel.department).publish();
		// @formatter:on
	}

	private void provider(ModelProducerFactoryBuilder builder) {
		WithType<Provider> provider = builder.type(Provider.class);
		// @formatter:off
		provider
			.credentials(SALE_CREDENTIAL, STOCK_CREDENTIAL, HEAD)
				.fields(_Provider.id, _Provider.name, _Provider.active, _Provider.productDetails).publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Provider.createdBy, _Provider.createdTimestamp,
						_Provider.updatedBy, _Provider.updatedTimestamp,
						_Provider.deactivatedTimestamp,
						_Provider.approvedBy, _Provider.approvedTimestamp,
						_Provider.email, _Provider.phoneNumbers, _Provider.address,
						_Provider.representatorName, _Provider.website)
				.publish();
		
		WithType<ProductProviderDetail> productDetail = builder.type(ProductProviderDetail.class);
		
		productDetail
			.credentials(SALE_CREDENTIAL, STOCK_CREDENTIAL, HEAD)
				.fields(_ProductProviderDetail.price,
						_ProductProviderDetail.productId, _ProductProviderDetail.providerId, 
						_ProductProviderDetail.product, _ProductProviderDetail.provider)
				.publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_ProductProviderDetail.droppedTimestamp, _ProductProviderDetail.approvedTimestamp,
						_ProductProviderDetail.createdBy, _ProductProviderDetail.createdTimestamp,
						_ProductProviderDetail.approvedBy)
				.publish();
		// @formatter:on
	}

	private Credential[] any() {
		return allPersonnelsAnd(ANONYMOUS, CUSTOMER, HEAD);
	}

	private void product(ModelProducerFactoryBuilder builder) {
		WithType<Product> product = builder.type(Product.class);
		// @formatter:off
		product
			.credentials(any())
				.fields(_Product.id, _Product.name, _Product.price,
						_Product.category, _Product.images, _Product.description,
						_Product.rating, _Product.stockDetails)
				.publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Product.createdBy, _Product.createdTimestamp,
						_Product.updatedBy, _Product.updatedTimestamp,
						_Product.active, _Product.deactivatedTimestamp,
						_Product.approvedBy, _Product.approvedTimestamp)
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
				.fields(_Category.products,
						_Category.createdBy, _Category.createdTimestamp,
						_Category.updatedBy, _Category.updatedTimestamp,
						_Category.active, _Category.deactivatedTimestamp,
						_Category.approvedBy, _Category.approvedTimestamp)
				.publish();
		// @formatter:on
	}

}
