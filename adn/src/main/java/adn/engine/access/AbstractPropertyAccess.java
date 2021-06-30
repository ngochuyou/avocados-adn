/**
 * 
 */
package adn.engine.access;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;

import adn.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;

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
	 * There are cases where field (if any then it's type is non-primitive type) or
	 * the setter method requires primitive parameter or could be passed under other
	 * type. This method will try to find to find setter method with possible
	 * alternative primitive type
	 * 
	 * @param ownerType
	 * @param field
	 * @param setterName
	 * @return the method
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
			Set<Class<?>> alternativeTypes = TypeHelper.RELATION_MAP.get(parameterType);

			if (alternativeTypes != null && !alternativeTypes.isEmpty()) {
				for (Class<?> alternative : alternativeTypes) {
					try {
						return ownerType.getDeclaredMethod(setterName, alternative);
					} catch (Exception any) {
						continue;
					}
				}
			}

			Map<Class<?>, Function<Object, Object>> alternativeResolvers = TypeHelper.TYPE_CONVERTER.get(parameterType);

			if (alternativeResolvers == null || alternativeResolvers.isEmpty()) {
				throw new NoSuchMethodException(String.format("Setter name [%s(%s)] not found in type [%s]", setterName,
						parameterType, ownerType.getName()));
			}

			for (Class<?> alternative : alternativeResolvers.keySet()) {
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

	@Override
	public String toString() {
		return String.format("%s(getter=[%s], setter=[%s])", this.getClass().getSimpleName(),
				getter == NoAccess.NO_OP_GETTER ? "<no_op>" : getter.getMethodName(),
				setter == NoAccess.NO_OP_SETTER ? "<no_op>" : setter.getMethodName());
	}

}
