package adn.security;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import adn.application.context.builders.CredentialFactory;
import adn.model.factory.authentication.Credential;
import adn.service.internal.Role;

public class UserDetailsImpl extends User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Role role;
	private final long version;
	private final Credential credential;

	public UserDetailsImpl(String username, String password, boolean enabled, boolean accountNonExpired,
			boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
			Role role, long version) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.role = role;
		this.version = version;
		this.credential = role;
	}

	public UserDetailsImpl(String username, String password, boolean isLocked,
			Collection<? extends GrantedAuthority> authorities, Role role, long version) {
		this(username, password, true, true, true, isLocked, authorities, role, version);
	}

	public UserDetailsImpl(String username, String password, boolean isLocked,
			Collection<? extends GrantedAuthority> authorities, Role role, UUID departmentId, long version) {
		super(username, password, true, true, true, isLocked, authorities);
		this.role = role;
		this.version = version;
		this.credential = CredentialFactory.partional(role, departmentId);
	}

	public Role getRole() {
		return role;
	}

	public long getVersion() {
		return version;
	}

	public Credential getCredential() {
		return credential;
	}

}
