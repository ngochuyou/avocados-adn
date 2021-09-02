/**
 * 
 */
package adn.model.entities;

import javax.persistence.Entity;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
public class Head extends Operator {

	public Head() {
		super();
	}

	public Head(String id) {
		setId(id);
		setRole(Role.HEAD);
	}

}
