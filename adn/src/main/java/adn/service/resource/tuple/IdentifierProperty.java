/**
 * 
 */
package adn.service.resource.tuple;

import java.lang.reflect.Member;

import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

import adn.service.resource.persister.IdentifierGenerator;

/**
 * @author Ngoc Huy
 *
 */
public class IdentifierProperty<X, Y> extends AbstractAttribute<X, Y> implements IdentifierAttribute<X, Y> {

	private final boolean virtual;
	private final boolean embedded;
	private final IdentifierValue unsavedValue;
	private final IdentifierGenerator identifierGenerator;
	private final boolean hasIdentifierMapper;

	// @formatter:off
	/**
	 * @param attributeName
	 * @param attributeType
	 */
	public IdentifierProperty(
			String attributeName,
			Type<Y> attributeType,
			boolean embedded,
			IdentifierValue unsavedValue,
			IdentifierGenerator identifierGenerator,
			ManagedType<X> declaringType) {
		super(attributeName, attributeType, declaringType, false, false, true, false);
		// TODO Auto-generated constructor stub
		this.virtual = false;
		this.embedded = embedded;
		this.hasIdentifierMapper = false;
		this.unsavedValue = unsavedValue;
		this.identifierGenerator = identifierGenerator;
	}
	// @formatter:on
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return getAttributeName();
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		// TODO Auto-generated method stub
		return PersistentAttributeType.BASIC;
	}

	@Override
	public ManagedType<X> getDeclaringType() {
		// TODO Auto-generated method stub
		return super.getDeclaringType();
	}

	@Override
	public Class<Y> getJavaType() {
		// TODO Auto-generated method stub
		return super.getAttributeType().getJavaType();
	}

	@Override
	public Member getJavaMember() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAssociation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCollection() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return virtual;
	}

	@Override
	public boolean isEmbedded() {
		// TODO Auto-generated method stub
		return embedded;
	}

	@Override
	public IdentifierGenerator getIdentifierGenerator() {
		// TODO Auto-generated method stub
		return identifierGenerator;
	}

	@Override
	public boolean hasIdentifierMapper() {
		// TODO Auto-generated method stub
		return hasIdentifierMapper;
	}

	@Override
	public IdentifierValue getUnsavedValue() {
		// TODO Auto-generated method stub
		return unsavedValue;
	}

}
