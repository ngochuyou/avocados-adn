/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.Id;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;
import org.springframework.util.Assert;

import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTuplizerImpl<T> implements ResourceTuplizer<T> {

	private final Getter idGetter;

	private final Setter idSetter;

	private final Class<T> type;

	/**
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * 
	 */
	public ResourceTuplizerImpl(Class<T> type, ResourceManager resourceManager)
			throws NoSuchMethodException, SecurityException {
		// TODO Auto-generated constructor stub
		Assert.notNull(type, "Resource type cannot be null");
		this.type = type;
		// Instantiate identifier getter/setter via @Id
		Field idField = null;

		for (Field f : type.getDeclaredFields()) {
			if (f.getDeclaredAnnotation(Id.class) != null) {
				idField = f;
				break;
			}
		}

		if (idField == null) {
			throw new NoSuchMethodException("Could not find resource identifier: " + type.getName());
		}

		Assert.isTrue(Serializable.class.isAssignableFrom(idField.getType()),
				"Resource identifier must be Serializable");

		Method method = type.getDeclaredMethod(
				Strings.toCamel("get " + idField.getName(), Strings.MULTIPLE_MATCHES_WHITESPACE_CHARS));

		idGetter = new GetterMethodImpl(type, idField.getName(), method);
		method = type.getDeclaredMethod(
				Strings.toCamel("set " + idField.getName(), Strings.MULTIPLE_MATCHES_WHITESPACE_CHARS),
				idField.getType());
		idSetter = new SetterMethodImpl(type, idField.getName(), method);
	}

	@Override
	public Serializable getId(T owner) {
		// TODO Auto-generated method stub
		return (Serializable) idGetter.get(owner);
	}

	@Override
	public Class<T> getType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Getter getIdGetter() {
		return idGetter;
	}

	@Override
	public Setter getIdSetter() {
		return idSetter;
	}

}
