/**
 * 
 */
package adn.model.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import adn.application.Common;
import adn.model.entities.metadata._Product;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "product_prices")
public class ProductPrice extends PermanentEntity implements ApprovableResource {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(columnDefinition = Common.UUID_MYSQL_COLUMN_DEFINITION, updatable = false)
	private UUID id;

	@Column(nullable = false, updatable = false, columnDefinition = Common.CURRENCY_MYSQL_COLUMN_DEFINITION)
	private BigDecimal price;

	@Column(updatable = false, name = "dropped_timestamp")
	private LocalDateTime droppedTimestamp;

	@Embedded
	private ApprovalInformations approvalInformations;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = _Product.$id, updatable = false)
	private Product product;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public LocalDateTime getDroppedTimestamp() {
		return droppedTimestamp;
	}

	public void setDroppedTimestamp(LocalDateTime droppedTimestamp) {
		this.droppedTimestamp = droppedTimestamp;
	}

	@Override
	public ApprovalInformations getApprovalInformations() {
		return approvalInformations;
	}

	public void setApprovalInformations(ApprovalInformations approvalInformations) {
		this.approvalInformations = approvalInformations;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

}
