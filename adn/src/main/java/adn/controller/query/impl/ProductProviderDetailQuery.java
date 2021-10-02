/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;
import java.util.Set;

import adn.model.entities.ProductCost;
import adn.model.entities.metadata._ProductCost;

/**
 * @author Ngoc Huy
 *
 */
public class ProductProviderDetailQuery extends AbstractPermanentEntityQuery<ProductCost> {

	private static final HashSet<String> ASSOCIATION_COLUMNS = new HashSet<>(Set.of(_ProductCost.product,
			_ProductCost.provider, _ProductCost.approvedBy));

	public ProductProviderDetailQuery() {
		super(ProductCost.class, ASSOCIATION_COLUMNS);
	}

	private ProductQuery product;
	private ProviderQuery provider;
	private UserQuery createdBy;
	private HeadQuery approvedBy;
//	private BooleanFilter approved;

	public ProductQuery getProduct() {
		return product;
	}

	public void setProduct(ProductQuery product) {
		this.product = product;
		setAssociated(product);
	}

	public ProviderQuery getProvider() {
		return provider;
	}

	public void setProvider(ProviderQuery provider) {
		this.provider = provider;
		setAssociated(provider);
	}

	public UserQuery getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserQuery createdBy) {
		this.createdBy = createdBy;
		setAssociated(createdBy);
	}

	public HeadQuery getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(HeadQuery approvedBy) {
		this.approvedBy = approvedBy;
		setAssociated(approvedBy);
	}

}
