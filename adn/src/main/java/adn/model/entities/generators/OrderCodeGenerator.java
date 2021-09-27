/**
 * 
 */
package adn.model.entities.generators;

import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

/**
 * @author Ngoc Huy
 *
 */
public class OrderCodeGenerator implements ValueGenerator<String> {

	public static final String NAME = "OrderCodeGenerator";
	public static final String PATH = "adn.model.entities.generators.OrderCodeGenerator";
	
	@Override
	public String generateValue(Session session, Object owner) {
		return UUID.randomUUID().toString();
	}

}
