/**
 * 
 */
package adn.model.entities;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Admin extends Account {

	@Column(name = "contract_date")
	protected LocalDate contractDate;

	@JsonIgnore
	public LocalDate getContractDate() {
		return contractDate;
	}

	public void setContractDate(LocalDate contractDate) {
		this.contractDate = contractDate;
	}

}
