/**
 * 
 */
package adn.model.models;

import adn.model.Modelized;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
@Modelized(relation = Account.class)
public class AccountModelForAdmin extends AccountModel {

	private String scope;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}
