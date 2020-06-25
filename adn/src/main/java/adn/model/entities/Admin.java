/**
 * 
 */
package adn.model.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Admin extends Account {

	@Column(name = "contract_date")
	protected Date contractDate;

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

}
