/**
 * 
 */
package adn.model.factory.extraction;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.application.context.ContextProvider;
import adn.helpers.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AccountRoleExtractor {

	Role extractRole(String jsonString);

	@Component
	class DefaultAccountRoleExtractor implements AccountRoleExtractor {

		private final ObjectMapper objectMapper;

		private final String roleFieldname = "role";

		/**
		 * 
		 */
		private DefaultAccountRoleExtractor() {
			// TODO Auto-generated constructor stub
			objectMapper = ContextProvider.getApplicationContext().getBean(ObjectMapper.class);
		}

		@Override
		public Role extractRole(String jsonString) {
			// TODO Auto-generated method stub
			try {
				return Role.valueOf(objectMapper.readTree(jsonString).get(roleFieldname).asText());
			} catch (Exception e) {
				return Role.ANONYMOUS;
			}
		}

	}

}
