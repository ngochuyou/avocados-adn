/**
 * 
 */
package adn.security;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;

import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class PersonnelDetails extends ApplicationUserDetails {

	private static final long serialVersionUID = -5649563972208557322L;

	private final UUID departmentId;

	public PersonnelDetails(String username, String password, boolean isLocked,
			Collection<? extends GrantedAuthority> authorities, Role role, long version, UUID departmentId) {
		super(username, password, isLocked, authorities, role, version);
		this.departmentId = departmentId;
	}

	public UUID getDepartmentId() {
		return departmentId;
	}

}
