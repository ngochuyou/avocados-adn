/**
 * 
 */
package adn.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.dao.Repository;
import adn.model.entities.Account;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
@Service(ApplicationUserDetailsService.NAME)
public class ApplicationUserDetailsService implements UserDetailsService {

	public static final String NAME = "applicationUserDetailsService";

	@Autowired
	private Repository repo;

	private static final String[] ATTRIBUTES = new String[] { Account.ID_FIELD_NAME, "password", Account.ROLE_FIELD_NAME,
			Account.VERSION_FIELD_NAME, Account.ACTIVE_FIELD_NAME };
	public static final ZoneId ZONE = ZoneId.systemDefault();

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Object[] account = repo.findById(username, Account.class, ATTRIBUTES);

		if (account == null) {
			throw new UsernameNotFoundException(String.format("%s not found", username));
		}

		Role role = (Role) account[2];

		return new ApplicationUserDetails((String) account[0], (String) account[1], (boolean) account[4],
				Set.of(new SimpleGrantedAuthority("ROLE_" + role)), role,
				((LocalDateTime) account[3]).atZone(ZONE).toEpochSecond());
	}

}
