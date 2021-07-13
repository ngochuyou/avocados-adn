/**
 * 
 */
package adn.model.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import adn.model.entities.constants.NamedSize;
import adn.model.entities.constants.Status;
import adn.model.entities.generators.StockDetailIdGenerator;

/**
 * @author Ngoc Huy
 *
 */
@javax.persistence.Entity
@Table(name = "stock_details")
public class StockDetail extends Entity {

	public static final int IDENTIFIER_LENGTH = 25;

	@Id
	@GeneratedValue(generator = StockDetailIdGenerator.NAME)
	@GenericGenerator(name = StockDetailIdGenerator.NAME, strategy = StockDetailIdGenerator.PATH)
	@Column(columnDefinition = "NVARCHAR(25)")
	private String id;

	@ManyToOne
	private Product product;

	@Enumerated
	@Column(columnDefinition = "NVARCHAR(4)")
	private NamedSize size;

	@Column(name = "numeric_size", columnDefinition = "TINYINT")
	private Integer numericSize;

	@Column(name = "stocked_date")
	private LocalDate stockedDate;

	@ManyToOne(fetch = FetchType.LAZY)
	private Personnel personnel;

	@Enumerated
	@Column(nullable = false, columnDefinition = "NVARCHAR(20)")
	private Status status;

	@Column(nullable = false)
	private Boolean active;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public NamedSize getSize() {
		return size;
	}

	public void setSize(NamedSize size) {
		this.size = size;
	}

	public Integer getNumericSize() {
		return numericSize;
	}

	public void setNumericSize(Integer numericSize) {
		this.numericSize = numericSize;
	}

	public LocalDate getStockedDate() {
		return stockedDate;
	}

	public void setStockedDate(LocalDate stockedDate) {
		this.stockedDate = stockedDate;
	}

	public Personnel getPersonnel() {
		return personnel;
	}

	public void setPersonnel(Personnel personnel) {
		this.personnel = personnel;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

}
