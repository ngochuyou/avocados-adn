/**
 * 
 */
package adn.service.resource.metamodel.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BasicType;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.Type;
import org.springframework.util.Assert;

import adn.service.resource.storage.LocalResourceStorage.ResourceResultSet;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractTranslatedBasicType implements BasicType {

	protected final BasicType basicType;

	protected AbstractTranslatedBasicType(BasicType basicType) {
		// TODO Auto-generated constructor stub
		Assert.notNull(basicType, "BasicType must not be null");
		this.basicType = basicType;
	}

	@Override
	public boolean isAssociationType() {
		// TODO Auto-generated method stub
		return basicType.isAssociationType();
	}

	@Override
	public boolean isCollectionType() {
		// TODO Auto-generated method stub
		return basicType.isCollectionType();
	}

	@Override
	public boolean isEntityType() {
		// TODO Auto-generated method stub
		return basicType.isEntityType();
	}

	@Override
	public boolean isAnyType() {
		// TODO Auto-generated method stub
		return basicType.isAnyType();
	}

	@Override
	public boolean isComponentType() {
		// TODO Auto-generated method stub
		return basicType.isComponentType();
	}

	@Override
	public int getColumnSpan(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return basicType.getColumnSpan(mapping);
	}

	@Override
	public int[] sqlTypes(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return basicType.sqlTypes(mapping);
	}

	@Override
	public Size[] dictatedSizes(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return basicType.dictatedSizes(mapping);
	}

	@Override
	public Size[] defaultSizes(Mapping mapping) throws MappingException {
		// TODO Auto-generated method stub
		return defaultSizes(mapping);
	}

	@Override
	public Class<?> getReturnedClass() {
		// TODO Auto-generated method stub
		return basicType.getReturnedClass();
	}

	@Override
	public boolean isSame(Object x, Object y) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.isSame(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.isEqual(x, y);
	}

	@Override
	public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.isEqual(x, y, factory);
	}

	@Override
	public int getHashCode(Object x) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.getHashCode(x);
	}

	@Override
	public int getHashCode(Object x, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.getHashCode(x, factory);
	}

	@Override
	public int compare(Object x, Object y) {
		// TODO Auto-generated method stub
		return basicType.compare(x, y);
	}

	@Override
	public boolean isDirty(Object old, Object current, SharedSessionContractImplementor session)
			throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.isDirty(old, current, session);
	}

	@Override
	public boolean isDirty(Object oldState, Object currentState, boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.isDirty(oldState, currentState, session);
	}

	@Override
	public boolean isModified(Object dbState, Object currentState, boolean[] checkable,
			SharedSessionContractImplementor session) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.isModified(dbState, currentState, checkable, session);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return basicType.nullSafeGet(rs, getName(), session, owner);
	}

	@Override
	public Object nullSafeGet(ResultSet rs, String name, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		return basicType.nullSafeGet(rs, name, session, owner);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable,
			SharedSessionContractImplementor session) throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		basicType.nullSafeSet(st, value, index, settable, session);
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {
		// TODO Auto-generated method stub
		basicType.nullSafeSet(st, value, index, session);
	}

	@Override
	public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.toLoggableString(value, factory);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return basicType.getName();
	}

	@Override
	public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.deepCopy(value, factory);
	}

	@Override
	public boolean isMutable() {
		// TODO Auto-generated method stub
		return basicType.isMutable();
	}

	@Override
	public Serializable disassemble(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.disassemble(value, session, owner);
	}

	@Override
	public Object assemble(Serializable cached, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.assemble(cached, session, owner);
	}

	@Override
	public void beforeAssemble(Serializable cached, SharedSessionContractImplementor session) {
		// TODO Auto-generated method stub
		basicType.beforeAssemble(cached, session);
	}

	@Override
	public Object resolve(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.resolve(value, session, owner);
	}

	@Override
	public Object semiResolve(Object value, SharedSessionContractImplementor session, Object owner)
			throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.semiResolve(value, session, owner);
	}

	@Override
	public Type getSemiResolvedType(SessionFactoryImplementor factory) {
		// TODO Auto-generated method stub
		return basicType.getSemiResolvedType(factory);
	}

	@Override
	public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner,
			@SuppressWarnings("rawtypes") Map copyCache) throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.replace(original, target, session, owner, copyCache);
	}

	@Override
	public Object replace(Object original, Object target, SharedSessionContractImplementor session, Object owner,
			@SuppressWarnings("rawtypes") Map copyCache, ForeignKeyDirection foreignKeyDirection)
			throws HibernateException {
		// TODO Auto-generated method stub
		return basicType.replace(original, target, session, owner, copyCache, foreignKeyDirection);
	}

	@Override
	public boolean[] toColumnNullness(Object value, Mapping mapping) {
		// TODO Auto-generated method stub
		return basicType.toColumnNullness(value, mapping);
	}

	protected ResourceResultSet assertResultSet(ResultSet rs) throws HibernateException {
		if (rs instanceof ResourceResultSet) {
			return (ResourceResultSet) rs;
		}

		throw new HibernateException("ResultSet must be instance of " + ResourceResultSet.class);
	}

}
