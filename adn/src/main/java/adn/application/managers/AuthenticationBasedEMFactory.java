/**
 * 
 */
package adn.application.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.model.Entity;
import adn.model.Model;
import adn.model.factory.Factory;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(4)
public class AuthenticationBasedEMFactory implements ApplicationManager, Factory {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		logger.info("Initializing " + this.getClass().getName());

		logger.info("Finished initializing " + this.getClass().getName());
	}

	@Override
	public <E extends Entity, M extends Model> M produce(E entity, Class<M> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E extends Entity, M extends Model> E produce(M model, Class<E> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

}
