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
import adn.model.entities.District;
import adn.model.entities.Item;
import adn.model.entities.Order;
import adn.model.entities.OrderDetail;
import adn.model.entities.Personnel;
import adn.model.entities.Product;
import adn.model.entities.ProductCost;
import adn.model.entities.ProductPrice;
import adn.model.entities.Provider;
import adn.model.entities.Province;
import adn.model.entities.User;
import adn.model.entities.id.ProductCostId;
import adn.model.entities.id.ProductPriceId;
import adn.model.entities.metadata._Category;
import adn.model.entities.metadata._Customer;
import adn.model.entities.metadata._District;
import adn.model.entities.metadata._Entity;
import adn.model.entities.metadata._Item;
import adn.model.entities.metadata._NamedResource;
import adn.model.entities.metadata._Order;
import adn.model.entities.metadata._OrderDetail;
import adn.model.entities.metadata._Personnel;
import adn.model.entities.metadata._Product;
import adn.model.entities.metadata._ProductCost;
import adn.model.entities.metadata._ProductPrice;
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
		user(builder);
		provider(builder);
		category(builder);
		product(builder);
		order(builder);
		adminDivision(builder);
	}

	private void adminDivision(ModelProducerFactoryBuilder builder) {
		WithType<Province> province = builder.type(Province.class);

		province.credentials(any()).fields(_Entity.id, _NamedResource.name).publish();

		WithType<District> district = builder.type(District.class);

		district.credentials(any()).fields(_Entity.id, _NamedResource.name, _District.province).publish();
	}

	private void user(ModelProducerFactoryBuilder builder) {
		WithType<User> user = builder.type(User.class);
		// @formatter:off
		user
			.credentials(authorized())
				.fields(_User.id).use(_User._id).publish()
				.fields(_User.firstName, _User.lastName, _User.photo,
						_User.role, _User.gender,
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
				.fields(_Personnel.createdBy, _Personnel.department).publish();
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private void provider(ModelProducerFactoryBuilder builder) {
		WithType<Provider> provider = builder.type(Provider.class);
		// @formatter:off
		provider
			.credentials(SALE_CREDENTIAL, STOCK_CREDENTIAL, HEAD)
				.fields(_Provider.id, _Provider.name, _Provider.productCosts).publish()
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
						_ProductCost.appliedTimestamp)
				.publish()
				.fields(_ProductCost.id)
					.useFunction((args, credential) -> {
						ProductCostId id = (ProductCostId) args.getSource();
						
						return Map.of(
								_ProductCost.productId, id.getProductId(),
								_ProductCost.providerId, id.getProviderId(),
								_ProductCost.appliedTimestamp, Utils.ldt(id.getAppliedTimestamp()),
								_ProductCost.droppedTimestamp, Utils.ldt(id.getDroppedTimestamp())
							);
					})
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_ProductCost.droppedTimestamp, _ProductCost.approvedTimestamp,
						_ProductCost.approvedBy)
				.publish();
		// @formatter:on
	}

	@SuppressWarnings("unchecked")
	private void product(ModelProducerFactoryBuilder builder) {
		WithType<Product> product = builder.type(Product.class);
		// @formatter:off
		product
			.credentials(any())
				.fields(_Product.id, _Product.name, _Product.category,
						_Product.images, _Product.description, _Product.code,
						_Product.rating, _Product.material)
				.publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Product.createdBy, _Product.createdDate,
						_Product.lastModifiedBy, _Product.lastModifiedDate,
						_Product.approvedBy, _Product.approvedTimestamp,
						_Product.locked)
				.publish();
		
		WithType<ProductPrice> price = builder.type(ProductPrice.class);
		
		price
			.credentials(any())
				.fields(_ProductPrice.price).publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_ProductPrice.productId, _ProductPrice.appliedTimestamp,
						_ProductPrice.droppedTimestamp, _ProductPrice.approvedBy, _ProductPrice.approvedTimestamp,
						_ProductPrice.product).publish()
				.fields(_ProductPrice.id)
					.useFunction((args, credential) -> {
						ProductPriceId id = (ProductPriceId) args.getSource();
						
						return Map.of(
								_ProductPrice.productId, id.getProductId(),
								_ProductPrice.appliedTimestamp, Utils.ldt(id.getAppliedTimestamp()),
								_ProductPrice.droppedTimestamp, Utils.ldt(id.getDroppedTimestamp())
							);
					});
		
		WithType<Item> item = builder.type(Item.class);

		item
			.credentials(any())
				.fields(_Item.id, _Item.code, _Item.namedSize,
						_Item.numericSize, _Item.color, _Item.status,
						_Item.product).publish()
			.credentials(CUSTOMER_SERVICE_CREDENTIAL, STOCK_CREDENTIAL, HEAD)
				.fields(_Item.note, _Item.cost, _Item.provider, _Item.auditInformations,
						_Item.createdDate, _Item.createdBy,
						_Item.lastModifiedDate, _Item.lastModifiedBy).publish();
		// @formatter:on
	}

	private void category(ModelProducerFactoryBuilder builder) {
		WithType<Category> category = builder.type(Category.class);
		// @formatter:off
		category
			.credentials(any())
				.fields(_Category.id, _Category.description, _Category.name,
						_Category.code)
				.publish()
			.credentials(SALE_CREDENTIAL, HEAD)
				.fields(_Category.products)
				.publish();
		// @formatter:on
	}

	private void order(ModelProducerFactoryBuilder builder) {
		WithType<Order> order = builder.type(Order.class);
		// @formatter:off
		order
			.credentials(owner(), HEAD, CUSTOMER_SERVICE_CREDENTIAL)
				.fields(_Order.id, _Order.code, _Order.status,
						_Order.address, _Order.district, _Order.deliveryFee,
						_Order.customer, _Order.updatedTimestamp,
						_Order.note, _Order.details, _Order.createdTimestamp).publish();
		// @formatter:on
		WithType<OrderDetail> details = builder.type(OrderDetail.class);
		// @formatter:off
		details
			.credentials(owner(), HEAD, CUSTOMER_SERVICE_CREDENTIAL)
				.fields(_OrderDetail.itemId, _OrderDetail.orderId, _OrderDetail.rating,
						_OrderDetail.price, _OrderDetail.order, _OrderDetail.item).publish();
		// @formatter:on
	}

	private Credential[] from(Credential... credentials) {
		return credentials;
	}

	private Credential[] and(Credential[]... credentialsSets) {
		return Stream.of(credentialsSets).flatMap(Stream::of).toArray(Credential[]::new);
	}

	private Credential[] personnel() {
		return CredentialFactory.allPersonnelsCredentials().toArray(Credential[]::new);
	}

	private Credential[] any() {
		return and(operator(), from(ANONYMOUS, CUSTOMER, owner()));
	}

	private Credential[] operator() {
		return and(personnel(), from(HEAD));
	}

	private Credential[] authorized() {
		return and(operator(), from(owner()));
	}

}
