/**
 * 
 */
package adn.model.entities;

import java.io.Serializable;

import adn.model.AbstractModel;

/**
 * @author Ngoc Huy
 *
 */
public abstract class Entity extends AbstractModel {

	public abstract void setId(Serializable id);

}
