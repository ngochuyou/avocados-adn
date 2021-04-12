/**
 * 
 */
package adn.service.resource.local;

import org.hibernate.service.Service;

import adn.helpers.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public interface NamingStrategy extends Service {

	public static final CamelCasedNamingStrategy DEFAULT_NAMING_STRATEGY = new CamelCasedNamingStrategy();

	String getName(Class<?> clazz);

	class CamelCasedNamingStrategy implements NamingStrategy {

		private static final long serialVersionUID = 1L;

		private CamelCasedNamingStrategy() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public String getName(Class<?> clazz) {
			// TODO Auto-generated method stub
			return StringHelper.toCamel(clazz.getSimpleName(), null);
		}

	}

}
