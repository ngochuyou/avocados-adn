/**
 * 
 */
package adn.controller.query.request;

import java.util.Arrays;
import java.util.List;

import adn.model.entities.metadata._Provider;

/**
 * @author Ngoc Huy
 *
 */
public class ProviderRequest extends AbstractColumnRequest {

	private ProductProviderDetailRequest productDetails = new ProductProviderDetailRequest();

	public ProductProviderDetailRequest getProductDetails() {
		return productDetails;
	}

	public void setProductDetails(ProductProviderDetailRequest productDetails) {
		this.productDetails = productDetails;
	}

	@Override
	public boolean hasAssociation() {
		return getColumns().contains(_Provider.productDetails) || !productDetails.isEmpty();
	}

	@Override
	protected List<List<String>> getAssociationColumns() {
		return Arrays.asList(productDetails.getColumns());
	}

	@Override
	public boolean isEmpty() {
		return getColumns().isEmpty() && productDetails.isEmpty();
	}

}
