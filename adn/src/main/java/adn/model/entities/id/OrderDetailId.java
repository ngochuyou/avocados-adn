/**
 * 
 */
package adn.model.entities.id;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import adn.application.Common;
import adn.model.entities.metadata._OrderDetail;

/**
 * @author Ngoc Huy
 *
 */
@Embeddable
public class OrderDetailId implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = _OrderDetail.$orderId, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger orderId;

	@Column(name = _OrderDetail.$itemId, nullable = false, columnDefinition = Common.MYSQL_BIGINT_COLUMN_DEFINITION)
	private BigInteger itemId;

	public OrderDetailId() {}

	public OrderDetailId(BigInteger orderId, BigInteger itemId) {
		this.orderId = orderId;
		this.itemId = itemId;
	}

	public BigInteger getOrderId() {
		return orderId;
	}

	public void setOrderId(BigInteger orderId) {
		this.orderId = orderId;
	}

	public BigInteger getItemId() {
		return itemId;
	}

	public void setItemId(BigInteger itemId) {
		this.itemId = itemId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemId, orderId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OrderDetailId other = (OrderDetailId) obj;
		return Objects.equals(itemId, other.itemId) && Objects.equals(orderId, other.orderId);
	}

}
