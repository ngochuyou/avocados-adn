/**
 * 
 */
package adn.model.factory.extraction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public interface AccountRoleExtractor {

	Role extractRole(String jsonString);

	@Component
	public class DefaultAccountRoleExtractor implements AccountRoleExtractor {

		private final ObjectMapper objectMapper;
		private final String roleFieldname = "role";

		@Autowired
		private DefaultAccountRoleExtractor(@Autowired ObjectMapper objectMapper) {
			this.objectMapper = objectMapper;
		}

		@Override
		public Role extractRole(String jsonString) {
			try {
				return Role.valueOf(objectMapper.readTree(jsonString).get(roleFieldname).asText());
			} catch (Exception any) {
				any.printStackTrace();
				return Role.ANONYMOUS;
			}
		}

	}

}
