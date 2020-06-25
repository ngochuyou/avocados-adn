/**
 * 
 */
package adn.model.models;

import adn.model.Modelized;
import adn.model.entities.Account;
import adn.model.entities.Personnel;

import java.util.Collection;

/**
 * @author Ngoc Huy
 *
 */
@Modelized(relation = Personnel.class)
public class PersonnelModel extends AccountModel {

	protected String createdBy;

	protected Collection<Account> accounts;
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

}
