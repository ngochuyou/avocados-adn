/**
 * 
 */
package adn.controller.query.request;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ngoc Huy
 *
 */
public class ProductProviderDetailRequest extends AbstractColumnRequest {

	private ProductRequest product = new ProductRequest();

	@Override
	public boolean hasAssociation() {
		return product != null;
	}

	@Override
	protected List<List<String>> getAssociationColumns() {
		return Arrays.asList(product.getColumns());
	}

	@Override
	public boolean isEmpty() {
		return getColumns().isEmpty() && product.isEmpty();
	}

	public ProductRequest getProduct() {
		return product;
	}

	public void setProduct(ProductRequest product) {
		this.product = product;
	}

}
