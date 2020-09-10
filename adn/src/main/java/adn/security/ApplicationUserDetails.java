package adn.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import adn.utilities.Role;

public class ApplicationUserDetails extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Role role;

	public ApplicationUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities, Role role) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		// TODO Auto-generated constructor stub
		this.role = role;
	}

	public ApplicationUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
			Role role) {
		super(username, password, true, true, true, true, authorities);
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

}
