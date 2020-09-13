/**
 * 
 */
package adn.model.models;

import java.util.Date;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import adn.model.Genetized;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Genetized(entityGene = Admin.class)
public class AdminModel extends AccountModel {

	protected Date contractDate;

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

}
