/**
 * 
 */
package adn.controller.query.request;

import static adn.model.entities.Provider.PRODUCT_DETAILS_FIELD;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adn.controller.query.BadColumnsRequestException;
import adn.controller.query.ColumnsRequest;
import io.jsonwebtoken.lang.Collections;

/**
 * @author Ngoc Huy
 *
 */
public class ProviderRequest implements ColumnsRequest {

	private static final String CONFLICTION_MESSAGE = String
			.format("Either request %s as a whole or specific columns of it, not both", PRODUCT_DETAILS_FIELD);

	private List<String> columns;
	private List<String> productDetailsColumns;

	private boolean isProductDetailsRequestedSpecifically = false;
	private boolean isProductDetailsRequestedCollectively = false;

	private ProductDetailsRequestMarker productDetailsRequestMarker = new ProductDetailsRequestMarker();

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) throws BadColumnsRequestException {
		if (columns.contains(PRODUCT_DETAILS_FIELD)) {
			if (productDetailsRequestMarker == null) {
				throw new BadColumnsRequestException(CONFLICTION_MESSAGE);
			}

			productDetailsRequestMarker.markCollectively();
		}

		this.columns = columns;
	}

	public List<String> getProductDetailsColumns() {
		return productDetailsColumns;
	}

	public void setProductDetailsColumns(List<String> stockDetailsColumns) throws BadColumnsRequestException {
		if (Collections.isEmpty(stockDetailsColumns)) {
			return;
		}

		if (productDetailsRequestMarker == null) {
			throw new BadColumnsRequestException(CONFLICTION_MESSAGE);
		}

		this.productDetailsColumns = stockDetailsColumns;
		productDetailsRequestMarker.markSpecifically();
	}

	public boolean isProductDetailsSpecificallyRequested() {
		return isProductDetailsRequestedSpecifically;
	}

	public boolean isProductDetailsCollectivelyRequested() {
		return isProductDetailsRequestedCollectively;
	}

	@Override
	public boolean isEmpty() {
		return Collections.isEmpty(columns) && Collections.isEmpty(productDetailsColumns);
	}

	@Override
	public Collection<String> join() {
		return Stream.concat(columns.stream(), productDetailsColumns.stream()).collect(Collectors.toList());
	}

	private class ProductDetailsRequestMarker {

		void markSpecifically() {
			isProductDetailsRequestedSpecifically = true;
			isProductDetailsRequestedCollectively = false;
			productDetailsRequestMarker = null;
		}

		void markCollectively() {
			isProductDetailsRequestedSpecifically = false;
			isProductDetailsRequestedCollectively = true;
			productDetailsRequestMarker = null;
		}

	}

}
