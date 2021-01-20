/**
 * 
 */
package adn.model.models;

import adn.model.AbstractModel;
import adn.model.Genetized;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Genetized(entityGene = Entity.class, modelGene = Model.class)
public abstract class Model extends AbstractModel {

	protected String id;

	abstract public String getId();

}