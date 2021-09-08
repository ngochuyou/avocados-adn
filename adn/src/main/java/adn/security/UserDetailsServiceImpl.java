/**
 * 
 */
package adn.security;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.dao.generic.GenericRepository;
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
	private GenericRepository repo;

	@Autowired
	private DepartmentService departmentService;

	private static final List<String> ATTRIBUTES = Arrays.asList(_Account.id, _Account.password, _Account.role,
			_Account.updatedDate, _Account.active);
	public static final ZoneId ZONE = ZoneId.systemDefault();

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Object[]> optional = repo.findById(Account.class, username, ATTRIBUTES);

		if (optional.isEmpty()) {
			throw new UsernameNotFoundException(String.format("%s not found", username));
		}

		Object[] cols = optional.get();
		Role role = (Role) cols[2];
		String id = (String) cols[0];
		String password = (String) cols[1];
		boolean activeState = (boolean) cols[4];
		Set<SimpleGrantedAuthority> auths = Set.of(new SimpleGrantedAuthority("ROLE_" + role));
		long version = ((LocalDateTime) cols[3]).atZone(ZONE).toEpochSecond();

		if (role == Role.PERSONNEL) {
			UUID departmentId = departmentService.getPersonnelDepartmentId(username);
			// @formatter:off
			return new PersonnelDetails(
					id,
					password,
					activeState,
					auths,
					role,
					version,
					departmentId);
		}

		return new UserDetailsImpl(
				id,
				password,
				activeState,
				auths,
				role,
				version);
		// @formatter:on
	}

}
