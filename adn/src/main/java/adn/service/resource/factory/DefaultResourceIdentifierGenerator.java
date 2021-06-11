/**
 * 
 */
package adn.service.resource.factory;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import adn.helpers.StringHelper;
import adn.service.resource.model.models.Resource;

/**
 * @author Ngoc Huy
 *
 */
public class DefaultResourceIdentifierGenerator implements IdentifierGenerator, Configurable {

	public static final DefaultResourceIdentifierGenerator INSTANCE = new DefaultResourceIdentifierGenerator();
	public static final String NAME = "resource_identifier";
	public static final String PATH = "adn.service.resource.factory.DefaultResourceIdentifierGenerator";

	public static final String IDENTIFIER_PARTS_SEPERATOR = "_";
	public static final int PARTS_AMOUNT = 2;

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		// @formatter:off
		if (object instanceof Resource) {
			Resource instance = (Resource) object;
			
			return new StringBuilder("" + new Date().getTime())
					.append(IDENTIFIER_PARTS_SEPERATOR)
					.append(StringHelper.hash(instance.getName()))
					.toString();
		}
		// @formatter:on
		return String.valueOf(new Date().getTime());
	}

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {}

	public static enum ResourceIdentifierPart {

		CREATION_TIMESTAMP("CREATION_TIMESTAMP", 0), HASHED_FILENAME("HASHED_FILENAME", 1);

		private final String partName;

		private final int partPos;

		ResourceIdentifierPart(String partName, int pos) {
			this.partName = partName;
			this.partPos = pos;
		}

		public String getPartName() {
			return partName;
		}

		public int getPartPos() {
			return partPos;
		}

		public static Date getCreationTimeStamp(String identifier) throws NumberFormatException, SQLException {
			String[] parts = identifier.split(IDENTIFIER_PARTS_SEPERATOR);

			if (parts.length != PARTS_AMOUNT) {
				throw new SQLException(
						String.format("Invalid Resource identifier value, expect [%s] part(s)", PARTS_AMOUNT));
			}

			return new Date(Long.valueOf(parts[CREATION_TIMESTAMP.getPartPos()]));
		}

	}

}
