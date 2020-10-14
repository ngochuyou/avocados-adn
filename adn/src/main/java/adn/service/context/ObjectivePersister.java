/**
 * 
 */
package adn.service.context;

import java.io.Serializable;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A persisting strategy that supports {@link Serializable} resource
 * 
 * @author Ngoc Huy
 *
 */
@Component("objectivePersister")
public class ObjectivePersister implements ResourcePersister {

	@Autowired
	private ServiceManager manager;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public ObjectiveResource<? extends Serializable> persist(Object o) throws PersistenceException {
		// TODO Auto-generated method stub
		String id = manager.generateResourceId().toString();

		logger.debug("Persisting resource of type: " + o.getClass() + " with identifier: " + id);

		return new ObjectiveResource<>((Serializable) o, id);
	}

	@Override
	public boolean supports(Object o) {
		// TODO Auto-generated method stub
		return o instanceof Serializable;
	}

}
