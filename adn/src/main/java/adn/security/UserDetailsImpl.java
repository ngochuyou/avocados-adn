package adn.security;

import java.time.ZoneId;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

	// @formatter:off
	public UserDetailsImpl(
			String username,
			String password,
			boolean enabled,
			boolean notExpired,
			boolean credentialsNotExpired,
			boolean notLocked,
			Collection<? extends GrantedAuthority> authorities,
			Role role,
			long version) {
		super(username, password, enabled, notExpired, credentialsNotExpired, notLocked, authorities);
		this.role = role;
		this.version = version;
		this.credential = role;
	}
	// @formatter:on
	public UserDetailsImpl(String username, String password, boolean notLocked,
			Collection<? extends GrantedAuthority> authorities, Role role, long version) {
		this(username, password, true, true, true, notLocked, authorities, role, version);
	}

	public UserDetailsImpl(String username, String password, boolean notLocked,
			Collection<? extends GrantedAuthority> authorities, Role role, UUID departmentId, long version) {
		super(username, password, true, true, true, notLocked, authorities);
		this.role = role;
		this.version = version;
		this.credential = CredentialFactory.partional(role, departmentId);
	}

	public UserDetailsImpl(adn.model.entities.User user) {
		this(user.getId(), user.getPassword(), user.isLocked(),
				Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())), user.getRole(),
				user.getUpdatedDate().atZone(ZoneId.systemDefault()).toEpochSecond());
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
