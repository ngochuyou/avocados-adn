/**
 * 
 */
package adn.security;

import java.time.LocalDate;
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

	private final String[] attributes = new String[] { "id", "password", "role", "updatedDate" };
	public static final ZoneId ZONE = ZoneId.systemDefault();

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Object[] account = repo.findById(username, Account.class, attributes);

		if (account == null) {
			throw new UsernameNotFoundException(String.format("%s not found", username));
		}

		Role role = (Role) account[2];

		return new ApplicationUserDetails((String) account[0], (String) account[1],
				Set.of(new SimpleGrantedAuthority("ROLE_" + role)), role,
				((LocalDate) account[3]).atStartOfDay(ZONE).toEpochSecond());
	}

}
