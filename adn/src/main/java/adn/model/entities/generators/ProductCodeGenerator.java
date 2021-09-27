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
public class ProductCodeGenerator implements ValueGenerator<String> {

	public static final String NAME = "ProductIdGenerator";
	public static final String PATH = "adn.model.entities.generators.ProductIdGenerator";

//	private static final String DELIMITER = "-";

//	@Override
//	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {}
//
//	@Override
//	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
//		Product product = (Product) object;
//		Category category;
//
//		if ((category = product.getCategory()) == null || category.getId() == null) {
//			throw new HibernateException("Category was null");
//		}
//
//		StringBuilder idBuilder = new StringBuilder(category.getId()).append(DELIMITER);
//		String name = String.valueOf(product.getName());
//
//		Assert.isTrue(StringHelper.hasLength(name), "Product name was empty");
//
//		int remainingSize = _Product.ID_LENGTH - idBuilder.length();
//		
//		name = StringHelper.removeSpaces(name);
//		idBuilder.append(name.length() >= remainingSize ? name.substring(0, remainingSize).toUpperCase()
//				: RandomStringUtils.randomAlphanumeric(remainingSize).toUpperCase());
//
//		return idBuilder.toString();
//	}

	@Override
	public String generateValue(Session session, Object owner) {
		return UUID.randomUUID().toString();
	}

}
