/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;
import java.util.Set;

import adn.controller.query.filter.UUIDFilter;
import adn.model.entities.Provider;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
public class ProviderQuery extends AbstractPermanentEntityQuery<Provider> {

	private static final HashSet<String> ASSOCIATION_COLUMNS = new HashSet<>(Set.of(_Provider.productCosts));

	private UUIDFilter id;

	private ProductProviderDetailQuery productDetails;

	public ProviderQuery() {
		super(Provider.class, ASSOCIATION_COLUMNS);
	}

	public UUIDFilter getId() {
		return id;
	}

	public void setId(UUIDFilter id) {
		this.id = id;
	}

	public ProductProviderDetailQuery getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(ProductProviderDetailQuery productDetails) {
		this.productDetails = productDetails;
		setAssociated(productDetails);
	}

	@Override
	public boolean hasCriteria() {
		return super.hasCriteria() || id != null;
	}

}
