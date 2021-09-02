/**
 * 
 */
package adn.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.dao.generic.Repository;
import adn.model.entities.Account;
import adn.model.entities.metadata._Account;
import adn.service.internal.Role;
import adn.service.services.DepartmentService;

/**
 * @author Ngoc Huy
 *
 */
@Service(UserDetailsServiceImpl.NAME)
public class UserDetailsServiceImpl implements UserDetailsService {

	public static final String NAME = "applicationUserDetailsService";

	@Autowired
	private Repository repo;

	@Autowired
	private DepartmentService departmentService;

	private static final String[] ATTRIBUTES = new String[] { _Account.id, _Account.password, _Account.role,
			_Account.updatedDate, _Account.active };
	public static final ZoneId ZONE = ZoneId.systemDefault();

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Object[] account = (Object[]) repo.findById(username, Account.class, ATTRIBUTES);

		if (account == null) {
			throw new UsernameNotFoundException(String.format("%s not found", username));
		}

		Role role = (Role) account[2];

		if (role == Role.PERSONNEL) {
			UUID departmentId = departmentService.getPersonnelDepartmentId(username);
			return new PersonnelDetails((String) account[0], (String) account[1], (boolean) account[4],
					Set.of(new SimpleGrantedAuthority("ROLE_" + role)), role,
					((LocalDateTime) account[3]).atZone(ZONE).toEpochSecond(), departmentId);
		}

		return new UserDetailsImpl((String) account[0], (String) account[1], (boolean) account[4],
				Set.of(new SimpleGrantedAuthority("ROLE_" + role)), role,
				((LocalDateTime) account[3]).atZone(ZONE).toEpochSecond());
	}

}
