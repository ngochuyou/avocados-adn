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
import adn.model.entities.constants.NamedSize;

/**
 * @author Ngoc Huy
 *
 */
public class StockDetailIdGenerator implements IdentifierGenerator, Configurable {

	public static final String NAME = "StockDetailIdGenerator";
	public static final String PATH = "adn.model.entities.generators.StockDetailIdGenerator";
	private static final String DELIMETER = "-";

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {

	}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		StockDetail details = (StockDetail) object;
		StringBuilder builder = new StringBuilder();
		Product product = details.getProduct();

		builder.append(product.getCategory().getCode());
		builder.append(DELIMETER);
		builder.append(product.getCode());
		builder.append(DELIMETER);
		builder.append(getSizeCode(details.getSize(), details.getNumericSize()));
		builder.append(DELIMETER);
		builder.append(RandomStringUtils.randomAlphabetic(StockDetail.IDENTIFIER_LENGTH - builder.length()));
		// @formatter:off
		return builder.toString();
		// @formatter:on
	}

	private String getSizeCode(NamedSize namedSize, Integer numericSize) {
		boolean noNamedSize, noNumericSize;

		if ((noNamedSize = (namedSize == null)) || (noNumericSize = (numericSize == null))) {
			throw new HibernateException("Product's size informations are missing");
		}

		return String.format("%s%s", noNamedSize ? "" : namedSize, noNumericSize ? "" : numericSize).toUpperCase();
	}

}
