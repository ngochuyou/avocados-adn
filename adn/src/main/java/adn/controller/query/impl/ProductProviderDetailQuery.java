/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;
import java.util.Set;

import adn.model.entities.ProductProviderDetail;
import adn.model.entities.metadata._ProductProviderDetail;

/**
 * @author Ngoc Huy
 *
 */
public class ProductProviderDetailQuery extends AbstractPermanentEntityQuery<ProductProviderDetail> {

	private static final HashSet<String> ASSOCIATION_COLUMNS = new HashSet<>(Set.of(_ProductProviderDetail.product,
			_ProductProviderDetail.provider, _ProductProviderDetail.createdBy, _ProductProviderDetail.approvedBy));

	public ProductProviderDetailQuery() {
		super(ProductProviderDetail.class, ASSOCIATION_COLUMNS);
	}

	private ProductQuery product;
	private ProviderQuery provider;
	private AccountQuery createdBy;
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

	public AccountQuery getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(AccountQuery createdBy) {
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
