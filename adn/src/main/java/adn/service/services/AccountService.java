package adn.service.services;

import java.util.Map;

import adn.model.entities.Account;
import adn.model.entities.Admin;
import adn.model.entities.Customer;
import adn.model.entities.Personnel;
import adn.service.internal.Role;
import adn.service.internal.Service;

@org.springframework.stereotype.Service
public class AccountService implements Service {

	public static final String UNKNOWN_USER_FIRSTNAME = "ANONYMOUS";
	public static final String UNKNOWN_USER_LASTNAME = "USER";
	public static final String MODEL_ID_FIELD_NAME = "username";
	
	// @formatter:off
	private final Map<Role, Class<? extends Account>> roleClassMap = Map.of(
			Role.ADMIN, Admin.class,
			Role.CUSTOMER, Customer.class,
			Role.PERSONNEL, Personnel.class,
			Role.ANONYMOUS, Account.class);
	// @formatter:on
	public static final String DEFAULT_ACCOUNT_PHOTO_NAME = "1619973416467_0c46022fcfda4d9f4bb8c09e8c42e9efc12d839d35c78c73e4dab1d24fac8a1c.png";

	@SuppressWarnings("unchecked")
	public <A extends Account> Class<A> getClassFromRole(Role role) {
		return (Class<A>) this.roleClassMap.get(role);
	}

	public <A extends Account> Role getRoleFromClass(Class<A> clazz) {
		// @formatter:off
		return this.roleClassMap
				.keySet().stream()
				.filter(key -> this.roleClassMap.get(key).equals(clazz))
				.findFirst().orElse(null);
		// @formatter:on
	}

}
