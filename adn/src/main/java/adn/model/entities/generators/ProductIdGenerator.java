/**
 * 
 */
package adn.model.entities.generators;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

import adn.helpers.StringHelper;
import adn.model.entities.Category;
import adn.model.entities.Product;

/**
 * @author Ngoc Huy
 *
 */
public class ProductIdGenerator implements IdentifierGenerator, Configurable {

	public static final String NAME = "ProductIdGenerator";
	public static final String PATH = "adn.model.entities.generators.ProductIdGenerator";

	private static final String DELIMITER = "-";

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		Product product = (Product) object;
		Category category;

		if ((category = product.getCategory()) == null || category.getId() == null) {
			throw new HibernateException("Category was null");
		}

		StringBuilder idBuilder = new StringBuilder(category.getId()).append(DELIMITER);
		String name = String.valueOf(product.getName());

		Assert.isTrue(StringHelper.hasLength(name), "Product name was empty");

		int remainingSize = Product.ID_LENGTH - idBuilder.length();
		
		name = StringHelper.removeSpaces(name);
		idBuilder.append(name.length() >= remainingSize ? name.substring(0, remainingSize).toUpperCase()
				: RandomStringUtils.randomAlphanumeric(remainingSize).toUpperCase());

		return idBuilder.toString();
	}

}
