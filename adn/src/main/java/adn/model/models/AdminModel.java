/**
 * 
 */
package adn.model.models;

import java.util.Date;

import adn.model.Genetized;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Genetized(gene = Admin.class)
public class AdminModel extends AccountModel {

	protected Date contractDate;

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

}
