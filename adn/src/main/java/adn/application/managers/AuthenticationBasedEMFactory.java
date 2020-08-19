/**
 * 
 */
package adn.application.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import adn.application.ApplicationManager;
import adn.model.entities.Entity;
import adn.model.factory.EntityExtractorFactory;
import adn.model.factory.Factory;
import adn.model.factory.production.security.AuthenticationBasedModelProducerFactory;
import adn.model.models.Model;
import adn.utilities.ClassReflector;
import adn.utilities.Role;

/**
 * @author Ngoc Huy
 *
 */
@Component
@Order(6)
public class AuthenticationBasedEMFactory implements ApplicationManager, Factory {

	@Autowired
	private EntityExtractorFactory extractorFactory;

	@Autowired
	private AuthenticationBasedModelProducerFactory producerFactory;

	private Map<Role, BiFunction<Entity, Class<Model>, ? extends Model>> functionMap;

	@Autowired
	private ClassReflector reflector;

	@Override
	public void initialize() throws Exception {
		// TODO Auto-generated method stub
		this.functionMap = new HashMap<>();
		this.functionMap.put(Role.ADMIN, this::produceForAdmin);
		this.functionMap.put(Role.CUSTOMER, this::produceForCustomer);
		this.functionMap.put(Role.PERSONNEL, this::produceForPersonnel);
		this.functionMap.put(Role.ANONYMOUS, this::produce);
		this.functionMap.put(null, this::produce);
	}

	@Override
	public <T extends Entity, M extends Model> T produce(M model, Class<T> clazz) {
		// TODO Auto-generated method stub
		return this.extractorFactory.getExtractor(clazz).extract(model, reflector.newInstanceOrNull(clazz));
	}

	@Override
	public <T extends Entity, M extends Model> M produce(T entity, Class<M> clazz) {
		// TODO Auto-generated method stub
		return this.producerFactory.getProducer(clazz).produce(entity, reflector.newInstanceOrNull(clazz));
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity, M extends Model> M produce(T entity, Class<M> clazz, Role role) {
		// TODO Auto-generated method stub
		return (M) this.functionMap.get(role).apply(entity, (Class<Model>) clazz);
	}

	public <T extends Entity, M extends Model> M produceForAdmin(T entity, Class<M> clazz) {
		return this.producerFactory.getProducer(clazz).produceForAdminAuthentication(entity,
				reflector.newInstanceOrNull(clazz));
	}

	public <T extends Entity, M extends Model> M produceForCustomer(T entity, Class<M> clazz) {
		return this.producerFactory.getProducer(clazz).produceForCustomerAuthentication(entity,
				reflector.newInstanceOrNull(clazz));
	}

	public <T extends Entity, M extends Model> M produceForPersonnel(T entity, Class<M> clazz) {
		return this.producerFactory.getProducer(clazz).produceForPersonnelAuthentication(entity,
				reflector.newInstanceOrNull(clazz));
	}

}