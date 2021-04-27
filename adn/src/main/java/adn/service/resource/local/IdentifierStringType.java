/**
 * 
 */
package adn.service.resource.local;

import static adn.helpers.FunctionHelper.reject;

import java.io.File;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BasicType;
import org.hibernate.type.DiscriminatorType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import adn.service.resource.storage.LocalResourceStorage;
import adn.service.resource.storage.LocalResourceStorage.ResourceResultSet;
import adn.service.resource.storage.LocalResourceStorage.SingleResourceResultSet;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class IdentifierStringType implements BasicType, DiscriminatorType<String> {

	public static final IdentifierStringType INSTANCE = new IdentifierStringType(StringType.INSTANCE);

	private final StringType stringType;

	private final Map<Class<?>, Function<Object, String>> hydrateFunctions = Map.of(File.class, this::fromFile);

	private IdentifierStringType(StringType stringType) {
		super();
		this.stringType = stringType;
	}

	private String fromFile(Object resource) {
		File file = (File) resource;

		return file.getPath().substring(LocalResourceStorage.IMAGE_FILE_DIRECTORY.length());
	}

	@Override
	public String stringToObject(String xml) throws Exception {
		// TODO Auto-generated method stub
		return stringType.stringToObject(xml);
	}

	@Override
	public boolean isAssociationType() {
		// TODO Auto-generated method stub
		return stringType.isAssociationType();
	}

	@Override
	public boolean isCollectionType() {
		// TODO Auto-generated method stub
		return stringType.isCollectionType();
	}

	@Override
	public boolean isEntityType() {
		// TODO Auto-generated method stub
		return stringType.isEntityType();
	}

	@Override
	public boolean isAnyType() {
		// TODO Auto-generated method stub
		return stringType.isAnyType();
	}

	@Override
	public boolean isComponentType() {
		// TODO Auto-generated method stub
		return stringType.isComponentType();
	}

	@Override
	public int getColumnSpan(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return stringType.getColumnSpan(mapping);
	}

	@Override
	public int[] sqlTypes(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return stringType.sqlTypes(mapping);
	}

	@Override
	public Size[] dictatedSizes(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return stringType.dictatedSizes(mapping);
	}

	@Override
	public Size[] defaultSizes(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return defaultSizes(mapping);
	}

	@Override
	public Class<?> getReturnedClass() {
		// TODO Auto-generated method stub
		return stringType.getReturnedClass();
	}

	@Override
	public boolean isSame(Object x, Object y) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.isSame(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.isEqual(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.isEqual(x, y, factory);
	}

	@Override
	public int getHashCode(Object x) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.getHashCode(x);
	}

	@Override
	public int getHashCode(Object x, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.getHashCode(x, factory);
	}

	@Override
	public int compare(Object x, Object y) {
		// TODO Auto-generated method stub
		return stringType.compare(x, y);
	}

	@Override
	public boolean isDirty(Object old, Object current, SharedSessionContractImplementor session)
			throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.isDirty(old, current, session);
	}

	@Override
	public boolean isDirty(Object oldState, Object currentState, boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.isDirty(oldState, currentState, session);
	}

	@Override
	public boolean isModified(Object dbState, Object currentState, boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.isModified(dbState, currentState, checkable, session);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return stringType.nullSafeGet(rs, getName(), session, owner);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String name, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return stringType.nullSafeGet(rs, name, session, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		stringType.nullSafeSet(st, value, index, settable, session);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		stringType.nullSafeSet(null, value, getName(), session);
	}

	@Override
	public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.toLoggableString(value, factory);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return stringType.getName();
	}

	@Override
	public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.deepCopy(value, factory);
	}

	@Override
	public boolean isMutable() {
		// TODO Auto-generated method stub
		return stringType.isMutable();
	}

	@Override
	public Serializable disassemble(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.disassemble(value, session, owner);
	}

	@Override
	public Object assemble(Serializable cached, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.assemble(cached, session, owner);
	}

	@Override
	public void beforeAssemble(Serializable cached, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		stringType.beforeAssemble(cached, session);
	}

	@Override
	public Object hydrate(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		if (rs instanceof ResourceResultSet) {
			ResourceResultSet resultSet = (ResourceResultSet) rs;
			Object row = resultSet instanceof SingleResourceResultSet
					? ((SingleResourceResultSet) resultSet).getObject(0)
					: resultSet.getCurrent();

			return hydrateFunctions.containsKey(resultSet.getResourceType())
					? hydrateFunctions.get(resultSet.getResourceType()).apply(row)
					: reject(new HibernateException(
							"Unable to hydrate identifier due to resource type is not supported: "
									+ resultSet.getResourceType()),
							HibernateException.class);
		}

		throw new HibernateException("ResultSet must be instance of " + ResourceResultSet.class);
	}

	@Override
	public Object resolve(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.resolve(value, session, owner);
	}

	@Override
	public Object semiResolve(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.semiResolve(value, session, owner);
	}

	@Override
	public Type getSemiResolvedType(SessionFactoryImplementor factory) {
		// TODO Auto-generated method stub
		return stringType.getSemiResolvedType(factory);
	}

	@Override
	public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner,
			@SuppressWarnings("rawtypes") Map copyCache) throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.replace(original, target, session, owner, copyCache);
	}

	@Override
	public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner,
			@SuppressWarnings("rawtypes") Map copyCache, ForeignKeyDirection foreignKeyDirection)
			throws HibernateException {
		// TODO Auto-generated method stub
		return stringType.replace(original, target, session, owner, copyCache, foreignKeyDirection);
	}

	@Override
	public boolean[] toColumnNullness(Object value, Mapping mapping) {
		// TODO Auto-generated method stub
		return stringType.toColumnNullness(value, mapping);
	}

	@Override
	public String objectToSQLString(String value, Dialect dialect) throws Exception {
		// TODO Auto-generated method stub
		return stringType.objectToSQLString(value, dialect);
	}

	@Override
	public String[] getRegistrationKeys() {
		// TODO Auto-generated method stub
		return new String[] { this.getClass().getSimpleName() };
	}

}
