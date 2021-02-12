/**
 * 
 */
package adn.service.resource.tuple;

import javax.persistence.metamodel.Attribute;

import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
public interface PropertyAccessBuildingStrategy {

	Getter buildGetter(Attribute<?, ?> attr) throws Exception;

	Setter buildSetter(Attribute<?, ?> attr) throws Exception;

	public static CamelCasedMethodPropertyAccessStrategy CAMEL_CASED_INSTANCE = new CamelCasedMethodPropertyAccessStrategy();

	class CamelCasedMethodPropertyAccessStrategy implements PropertyAccessBuildingStrategy {

		@Override
		public Getter buildGetter(Attribute<?, ?> attr) throws NoSuchMethodException, SecurityException {
			// TODO Auto-generated method stub
			// @formatter:off
			return new GetterMethod<>(attr.getJavaType(),
					attr.getDeclaringType().getJavaType()
						.getDeclaredMethod(Strings.toCamel("get " + attr.getName(), " ")));
			// @formatter:on
		}

		@Override
		public Setter buildSetter(Attribute<?, ?> attr) throws NoSuchMethodException, SecurityException {
			// TODO Auto-generated method stub
			// @formatter:off
			return new SetterMethod(attr.getDeclaringType().getJavaType()
										.getDeclaredMethod(Strings.toCamel("set " + attr.getName(), " "),
									attr.getJavaType()));
			// @formatter:on
		}

	}

}
