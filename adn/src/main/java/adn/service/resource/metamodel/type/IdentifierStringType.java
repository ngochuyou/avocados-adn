/**
 * 
 */
package adn.service.resource.metamodel.type;

import static adn.helpers.FunctionHelper.reject;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.IdentifierType;
import org.hibernate.type.StringType;

import adn.service.resource.storage.LocalResourceStorage;
import adn.service.resource.storage.LocalResourceStorage.ResourceResultSet;
import adn.service.resource.storage.LocalResourceStorage.SingleResourceResultSet;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class IdentifierStringType extends AbstractTranslatedBasicType implements IdentifierType<String> {

	public static final IdentifierStringType INSTANCE = new IdentifierStringType(StringType.INSTANCE);

	private final Map<Class<?>, Function<Object, String>> hydrateFunctions = Map.of(File.class, this::fromFile);

	private IdentifierStringType(StringType stringType) {
		super(stringType);
	}

	@Override
	public String stringToObject(String xml) throws Exception {
		// TODO Auto-generated method stub
		return ((StringType) basicType).stringToObject(xml);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		ResourceResultSet resultSet = assertResultSet(rs);
		Object row = resultSet instanceof SingleResourceResultSet ? ((SingleResourceResultSet) resultSet).getObject(0)
				: resultSet.getCurrent();

		return hydrateFunctions.containsKey(resultSet.getResourceType())
				? hydrateFunctions.get(resultSet.getResourceType()).apply(row)
				: reject(new HibernateException("Unable to hydrate identifier due to resource type is not supported: "
						+ resultSet.getResourceType()), HibernateException.class);
	}

	@Override
	public String[] getRegistrationKeys() {
		// TODO Auto-generated method stub
		return new String[] { this.getClass().getSimpleName() };
	}

	private String fromFile(Object resource) {

		return ((File) resource).getPath().substring(LocalResourceStorage.IMAGE_FILE_DIRECTORY.length());
	}

}
