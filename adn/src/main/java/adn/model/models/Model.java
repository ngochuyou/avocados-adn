/**
 * 
 */
package adn.model.models;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import adn.model.AbstractModel;
import adn.model.Genetized;
import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Genetized(entityGene = Entity.class, modelGene = Model.class)
public abstract class Model extends AbstractModel {

	protected String id;

	abstract public String getId();

}