/**
 * 
 */
package adn.controller.query.impl;

import adn.helpers.CollectionHelper;
import adn.model.entities.User;

/**
 * @author Ngoc Huy
 *
 */
public class AccountQuery extends AbstractPermanentEntityQuery<User> {

	public AccountQuery() {
		super(User.class, CollectionHelper.emptyHashSet());
	}
	
}
