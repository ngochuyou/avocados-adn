/**
 * 
 */
package adn.model.models;

import java.io.Serializable;

import adn.model.AbstractModel;
import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Entity.class, modelGene = Model.class)
public abstract class Model extends AbstractModel {

	protected Serializable id;

	public Serializable getId() {
		return id;
	}

	public void setId(Serializable id) {
		this.id = id;
	}

}