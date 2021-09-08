/**
 * 
 */
package adn.controller.query.impl;

import adn.helpers.CollectionHelper;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
public class AccountQuery extends AbstractPermanentEntityQuery<Account> {

	public AccountQuery() {
		super(Account.class, CollectionHelper.emptyHashSet());
	}
	
}
