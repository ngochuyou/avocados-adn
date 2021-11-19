/**
 * 
 */
package adn.model.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Entity
@Table(name = "heads")
public class Head extends Operator {

	public Head() {
		super();
	}

	public Head(String id) {
		setId(id);
		setRole(Role.HEAD);
	}

}
