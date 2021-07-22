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

import adn.model.entities.Product;
import adn.model.entities.StockDetail;

/**
 * @author Ngoc Huy
 *
 */
public class StockDetailIdGenerator implements IdentifierGenerator, Configurable {

	public static final String NAME = "StockDetailIdGenerator";
	public static final String PATH = "adn.model.entities.generators.StockDetailIdGenerator";
	private static final String DELIMITER = "-";

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {

	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		StockDetail details = (StockDetail) object;
		StringBuilder builder = new StringBuilder();
		Product product = details.getProduct();

		builder.append(product.getId());
		builder.append(DELIMITER);

		int remainingSize = StockDetail.IDENTIFIER_LENGTH - builder.length();

		builder.append(RandomStringUtils.randomAlphabetic(remainingSize));

		return builder.toString();
	}

}
