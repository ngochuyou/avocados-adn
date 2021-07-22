/**
 * 
 */
package adn.model.factory;

import java.util.List;
import java.util.UUID;

/**
 * @author Ngoc Huy
 *
 */
public interface DepartmentBasedModelProducer<S, P> extends ModelProducer<S, P> {

	P produce(S source, UUID departmentId);

	List<P> produce(List<S> source, UUID departmentId);

}
