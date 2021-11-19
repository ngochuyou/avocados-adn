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
public class UserQuery extends AbstractPermanentEntityQuery<User> {

	public UserQuery() {
		super(User.class, CollectionHelper.emptyHashSet());
	}
	
}
