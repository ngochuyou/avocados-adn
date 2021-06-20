package adn.service.services;

import java.util.Map;

import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.service.Service;

@org.springframework.stereotype.Service
public class AccountService implements Service {

	public static final String UNKNOWN_USER_FIRSTNAME = "ANONYMOUS";

	public static final String UNKNOWN_USER_LASTNAME = "USER";
	// @formatter:off
	private final Map<Role, Class<? extends Account>> roleClassMap = Map.of(
			Role.ADMIN, Admin.class,
			Role.CUSTOMER, Customer.class,
			Role.PERSONNEL, Personnel.class,
			Role.ANONYMOUS, Account.class);
	// @formatter:on
	@SuppressWarnings("unchecked")
	public <T extends Account> Class<T> getClassFromRole(Role role) {
		return (Class<T>) this.roleClassMap.get(role);
	}

	public <T extends Account> Role getRoleFromClass(Class<T> clazz) {
		// @formatter:off
		return this.roleClassMap
				.keySet().stream()
				.filter(key -> this.roleClassMap.get(key).equals(clazz))
				.findFirst().orElse(null);
		// @formatter:on
	}

}
