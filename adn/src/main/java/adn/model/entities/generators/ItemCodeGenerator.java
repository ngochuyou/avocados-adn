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
public class ItemCodeGenerator implements ValueGenerator<String> {

	public static final String NAME = "StockDetailIdGenerator";
	public static final String PATH = "adn.model.entities.generators.StockDetailIdGenerator";
//	private static final String DELIMITER = "-";

//	@Override
//	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
//
//	}
//
//	@Override
//	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
//		Item details = (Item) object;
//		StringBuilder builder = new StringBuilder();
//		Product product = details.getProduct();
//
//		builder.append(product.getId());
//		builder.append(DELIMITER);
//
//		int remainingSize = _StockDetail.IDENTIFIER_LENGTH - builder.length();
//
//		builder.append(RandomStringUtils.randomAlphanumeric(remainingSize).toUpperCase());
//
//		return builder.toString();
//	}

	@Override
	public String generateValue(Session session, Object owner) {
		return UUID.randomUUID().toString();
	}

}
