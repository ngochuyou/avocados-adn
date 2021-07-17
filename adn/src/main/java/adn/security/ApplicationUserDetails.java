package adn.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import adn.service.internal.Role;

public class ApplicationUserDetails extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Role role;
	private final long version;

	public ApplicationUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
			Role role, long version) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		// TODO Auto-generated constructor stub
		this.role = role;
		this.version = version;
	}

	public ApplicationUserDetails(String username, String password, boolean isLocked, Collection<? extends GrantedAuthority> authorities,
			Role role, long version) {
		super(username, password, true, true, true, isLocked, authorities);
		this.role = role;
		this.version = version;
	}

	public Role getRole() {
		return role;
	}

	public long getVersion() {
		return version;
	}

}
