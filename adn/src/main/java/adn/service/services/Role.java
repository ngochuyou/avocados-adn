/**
 * 
 */
package adn.service.services;

import java.util.Map;
import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public enum Role {
	ANONYMOUS, ADMIN, CUSTOMER, PERSONNEL;
	// @formatter:off
	private static final Map<Role, Set<Role>> MODIFICATION_ACCESS_MAP = Map.of(
			Role.ADMIN, Set.of(Role.ADMIN, Role.CUSTOMER, Role.PERSONNEL),
			Role.CUSTOMER, Set.of(Role.CUSTOMER),
			Role.PERSONNEL, Set.of(Role.PERSONNEL, Role.CUSTOMER),
			Role.ANONYMOUS, Set.of(Role.CUSTOMER)
	);
	// @formatter:on
	public boolean canModify(Role requested) {
		return MODIFICATION_ACCESS_MAP.get(this).contains(requested);
	}

}
