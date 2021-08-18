/**
 * 
 */
package adn.dao.specification;

import java.util.List;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface Selections<X extends Entity> {

	List<Selection<?>> toSelections(Root<X> root);

}
