/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.service.resource.engine.access.migrate.PropertyAccessStrategyFactory.PropertyAccessImplementor;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractPropertyAccess implements PropertyAccessImplementor {

	private final Getter getter;
	private final Setter setter;

	AbstractPropertyAccess(Getter getter, Setter setter) {
		this.getter = getterOrNoAccess(getter);
		this.setter = setterOrNoAccess(setter);
	}

	@Override
	public Getter getGetter() {
		return getter;
	}

	@Override
	public Setter getSetter() {
		return setter;
	}

	private Getter getterOrNoAccess(Getter instance) {
		return Optional.ofNullable(instance).orElse(NoAccess.NO_OP_GETTER);
	}

	private Setter setterOrNoAccess(Setter instance) {
		return Optional.ofNullable(instance).orElse(NoAccess.NO_OP_SETTER);
	}

	/**
	 * Try the following two types of getter method: </br>
	 * 1. Lead by "get": getXXX(); </br>
	 * 2. Lead by "is": isXXX();
	 * 
	 * @param ownerType
	 * @param fieldName
	 * @return
	 * @throws NoSuchMethodException
	 */
	protected static Method bestGuessGetter(Class<?> ownerType, String fieldName) throws NoSuchMethodException {
		String leadingGetMethodName = StringHelper.toCamel(String.format("%s %s", "get", fieldName),
				StringHelper.ONE_OF_WHITESPACE_CHARS);
		Method getterMethod;

		try {
			getterMethod = ownerType.getDeclaredMethod(leadingGetMethodName);
		} catch (NoSuchMethodException nsme) {
			String leadingIsMethodName = StringHelper.toCamel(String.format("%s %s", "is", fieldName),
					StringHelper.ONE_OF_WHITESPACE_CHARS);

			try {
				getterMethod = ownerType.getDeclaredMethod(leadingIsMethodName);
			} catch (NoSuchMethodException innerNSME) {
				throw innerNSME;
			}
		}

		return getterMethod;
	}

	/**
	 * There are cases where field (if any) type is non-primitive parameter while
	 * the setter method requires primitive parameter. This method will try to find
	 * to find setter method with possible alternative primitive type
	 * 
	 * @param ownerType
	 * @param field
	 * @param setterName
	 * @return
	 * @throws NoSuchMethodException when no alternative could be found
	 */
	protected static Method bestGuessSetter(Class<?> ownerType, Field field, String setterName,
			Class<?>... parameterTypes) throws NoSuchMethodException {
		// @formatter:off
		Class<?> parameterType = field != null ? field.getType() // prioritise field type
				: Optional.ofNullable(parameterTypes.length == 0 ? null : parameterTypes[0])
					.orElseThrow(() -> new NoSuchMethodException(String.format("Unable to best guess setter name [%s] in type [%s] since field presented and parameter types are empty", setterName, ownerType.getName())));
		// @formatter:on
		try {
			return ownerType.getDeclaredMethod(setterName, parameterType);
		} catch (NoSuchMethodException e) {
			Set<Class<?>> alternativeTypes = TypeHelper.NON_PRIMITIVE_RELATION_MAP.get(parameterType);

			if (alternativeTypes == null || alternativeTypes.isEmpty()) {
				throw new NoSuchMethodException(String.format("Setter name [%s(%s)] not found in type [%s]", setterName,
						field.getType().getName(), ownerType.getName()));
			}

			for (Class<?> alternative : alternativeTypes) {
				try {
					return ownerType.getDeclaredMethod(setterName, alternative);
				} catch (Exception any) {
					continue;
				}
			}

			throw new NoSuchMethodException(String.format("Setter name [%s(%s)] not found in type [%s]", setterName,
					field.getType().getName(), ownerType.getName()));
		}
	}

}
