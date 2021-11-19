/**
 * 
 */
package adn.controller.query.impl;

import java.util.HashSet;
import java.util.Set;

import adn.controller.query.filter.StringFilter;
import adn.model.entities.Provider;
import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
public class ProviderQuery extends AbstractPermanentEntityQuery<Provider> {

	private static final HashSet<String> ASSOCIATION_COLUMNS = new HashSet<>(Set.of(_Provider.productCosts));

	private StringFilter name;

	private ProductProviderDetailQuery productDetails;

	public ProviderQuery() {
		super(Provider.class, ASSOCIATION_COLUMNS);
	}

	public StringFilter getName() {
		return name;
	}

	public void setName(StringFilter name) {
		this.name = name;
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
		return super.hasCriteria() || name != null;
	}

}
