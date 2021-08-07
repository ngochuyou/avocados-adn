/**
 * 
 */
package adn.service.internal;

import java.util.Map;
import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public enum Role implements RoleDefinition {

	ANONYMOUS, ADMIN, CUSTOMER, PERSONNEL;

	// @formatter:off
	private static final Set<Role> EMPTY_ROLE_SET = Set.of();

	private static final Map<Role, Set<Role>> MODIFICATION_ACCESS_MAP;
	private static final Map<Role, Set<Role>> READ_ACCESS_MAP;
	private static final Map<Role, Set<Role>> UPDATABLE_ROLE_MAP = Map.of(
			Role.ADMIN, EMPTY_ROLE_SET,
			Role.PERSONNEL, EMPTY_ROLE_SET,
			Role.CUSTOMER, EMPTY_ROLE_SET,
			Role.ANONYMOUS, EMPTY_ROLE_SET
	);
	// @formatter:on

	static {
		Set<Role> all = Set.of(Role.ADMIN, Role.CUSTOMER, Role.PERSONNEL);
		Set<Role> customerAndPersonnel = Set.of(Role.PERSONNEL, Role.CUSTOMER);
		Set<Role> customer = Set.of(Role.CUSTOMER);
		Set<Role> customerEmployeeManager = Set.of(Role.PERSONNEL, Role.CUSTOMER);
		// @formatter:off
		MODIFICATION_ACCESS_MAP = Map.of(
				Role.ADMIN, all,
				Role.PERSONNEL, customerAndPersonnel,
				Role.CUSTOMER, customer,
				Role.ANONYMOUS, customer);
		READ_ACCESS_MAP = Map.of(
				Role.ADMIN, all,
				Role.PERSONNEL, customerAndPersonnel,
				Role.CUSTOMER, customerEmployeeManager,
				Role.ANONYMOUS, customerEmployeeManager);
		// @formatter:on
	}

	@Override
	public boolean canModify(Role requested) {
		return MODIFICATION_ACCESS_MAP.get(this).contains(requested);
	}

	@Override
	public boolean canRead(Role requested) {
		return READ_ACCESS_MAP.get(this).contains(requested);
	}

	@Override
	public boolean canBeUpdatedTo(Role requested) {
		return UPDATABLE_ROLE_MAP.get(this).contains(requested);
	}

}
