/**
 * 
 */
package adn.model.models;

import java.util.Date;

import adn.model.Modelized;
import adn.model.entities.Admin;

/**
 * @author Ngoc Huy
 *
 */
@Modelized(relation = Admin.class)
public class AdminModel extends AccountModel {

	protected Date contractDate;

	public Date getContractDate() {
		return contractDate;
	}

	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

}
