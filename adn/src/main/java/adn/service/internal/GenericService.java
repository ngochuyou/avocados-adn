/**
 * 
 */
package adn.service.internal;

import java.io.Serializable;

import adn.application.Result;
import adn.model.entities.ApprovableResource;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericService extends Service {

	<T extends ApprovableResource, ID extends Serializable> Result<T> approve(Class<T> type, ID resourceId, boolean flushOnFinish);

}
