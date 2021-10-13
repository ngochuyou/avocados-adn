/**
 * 
 */
package adn.model.entities.id;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import adn.application.Common;
import adn.helpers.Utils;
import adn.model.entities.metadata._ProductPrice;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
@Embeddable
public class ProductPriceId implements Serializable {

	@Column(name = _ProductPrice.$productId, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger productId;

	@Column(name = _ProductPrice.$appliedTimestamp, nullable = false)
	private LocalDateTime appliedTimestamp;

	@Column(name = _ProductPrice.$droppedTimestamp, nullable = false)
	private LocalDateTime droppedTimestamp;

	public ProductPriceId() {}

	public ProductPriceId(BigInteger productId, LocalDateTime appliedTimestamp, LocalDateTime dropedTimestamp) {
		super();
		this.productId = productId;
		this.appliedTimestamp = appliedTimestamp;
		this.droppedTimestamp = dropedTimestamp;
	}

	public BigInteger getProductId() {
		return productId;
	}

	public void setProductId(BigInteger productId) {
		this.productId = productId;
	}

	public LocalDateTime getAppliedTimestamp() {
		return appliedTimestamp;
	}

	public void setAppliedTimestamp(LocalDateTime appliedTimestamp) {
		this.appliedTimestamp = appliedTimestamp;
	}

	public LocalDateTime getDroppedTimestamp() {
		return droppedTimestamp;
	}

	public void setDroppedTimestamp(LocalDateTime droppedTimestamp) {
		this.droppedTimestamp = droppedTimestamp;
	}

	@Override
	public int hashCode() {
		int hash = 17;

		hash += 37 * productId.hashCode();
		hash += 37 * appliedTimestamp.hashCode();
		hash += 37 * droppedTimestamp.hashCode();

		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		ProductPriceId other = (ProductPriceId) obj;

		return Objects.equals(appliedTimestamp, other.appliedTimestamp)
				&& Objects.equals(droppedTimestamp, other.droppedTimestamp)
				&& Objects.equals(productId, other.productId);
	}

	@Override
	public String toString() {
		return String.format("Price for product [%s], applied on [%s] and dropped on [%s]", productId,
				Utils.ldt(appliedTimestamp), Utils.ldt(droppedTimestamp));
	}

}
