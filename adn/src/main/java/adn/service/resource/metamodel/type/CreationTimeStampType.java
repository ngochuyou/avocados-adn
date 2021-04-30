/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.TimestampType;

import adn.service.resource.local.ResourceManager;
import adn.service.resource.local.ResourcePersister;
import adn.service.resource.metamodel.DefaultResourceIdentifierGenerator.ResourceIdentifierPart;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class CreationTimeStampType extends AbstractTimestampType {

	public static final CreationTimeStampType INSTANCE = new CreationTimeStampType(TimestampType.INSTANCE);

	private CreationTimeStampType(TimestampType basicType) {
		super(basicType);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		ResourceManager rm = assertSession(session);
		Object row = getCurrentRow(rs);

		if (row instanceof File) {
			if (owner instanceof ResourcePersister) {
				return fromFile(rs, (ResourcePersister<?>) owner, rm);
			}

			throw new HibernateException("Owner type must be instance of " + ResourcePersister.class);
		}

		throw new HibernateException("Unsupported row type " + row.getClass());
	}

	@Override
	public String[] getRegistrationKeys() {
		return new String[] { CreationTimestamp.class.getName() };
	}

	private Date fromFile(ResultSet rs, ResourcePersister<?> persister, ResourceManager manager)
			throws HibernateException, SQLException {
		Object identifier = persister.getIdentifierType().hydrate(rs, null, manager, persister);

		if (identifier instanceof String) {
			return ResourceIdentifierPart.getCreationTimeStamp((String) identifier);
		}

		throw new HibernateException(
				"Unable to hydrate timestamp from Identifier, expect Identifier to be String type");
	}

}
