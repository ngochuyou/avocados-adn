/**
 * 
 */
package adn.service;

import java.util.Map;
import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public enum Role implements AccessDefinition {
	ANONYMOUS, ADMIN, CUSTOMER, PERSONNEL;

	// @formatter:off
	private static final Map<Role, Set<Role>> MODIFICATION_ACCESS_MAP = Map.of(
			Role.ADMIN, Set.of(Role.ADMIN, Role.CUSTOMER, Role.PERSONNEL),
			Role.CUSTOMER, Set.of(Role.CUSTOMER),
			Role.PERSONNEL, Set.of(Role.PERSONNEL, Role.CUSTOMER),
			Role.ANONYMOUS, Set.of(Role.CUSTOMER)
	);
	private static final Map<Role, Set<Role>> READ_ACCESS_MAP = Map.of(
			Role.ADMIN, Set.of(Role.ADMIN, Role.CUSTOMER, Role.PERSONNEL),
			Role.CUSTOMER, Set.of(Role.CUSTOMER),
			Role.PERSONNEL, Set.of(Role.PERSONNEL, Role.CUSTOMER),
			Role.ANONYMOUS, Set.of(Role.CUSTOMER)
	);
	// @formatter:on
	@Override
	public boolean canModify(Role requested) {
		return MODIFICATION_ACCESS_MAP.get(this).contains(requested);
	}

	@Override
	public boolean canRead(Role requested) {
		return READ_ACCESS_MAP.get(this).contains(requested);
	}

}
