/**
 * 
 */
package adn.model.models;

import adn.model.AbstractModel;
import adn.model.Generic;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Entity.class, modelGene = Model.class)
public abstract class Model extends AbstractModel {

	protected String id;

	abstract public String getId();

}