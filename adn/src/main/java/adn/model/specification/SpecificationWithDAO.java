/**
 * 
 */
package adn.model.specification;

import adn.application.ApplicationContextProvider;
import adn.dao.BaseDAO;
import adn.model.Model;

/**
 * @author Ngoc Huy
 *
 */
public interface SpecificationWithDAO<T extends Model> extends Specification<T> {

	final BaseDAO dao = ApplicationContextProvider.getApplicationContext().getBean(BaseDAO.class);

}
