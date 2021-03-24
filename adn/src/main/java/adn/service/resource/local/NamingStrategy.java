/**
 * 
 */
package adn.service.resource.local;

import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
public interface NamingStrategy extends Service {

	public static final CamelCasedNamingStrategy DEFAULT_NAMING_STRATEGY = new CamelCasedNamingStrategy();

	String getName(Class<?> clazz);

	class CamelCasedNamingStrategy implements NamingStrategy {

		private CamelCasedNamingStrategy() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public String getName(Class<?> clazz) {
			// TODO Auto-generated method stub
			return Strings.toCamel(clazz.getSimpleName(), null);
		}

	}

}
