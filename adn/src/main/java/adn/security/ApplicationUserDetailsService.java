/**
 * 
 */
package adn.security;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.dao.BaseRepository;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
@Service(ApplicationUserDetailsService.NAME)
public class ApplicationUserDetailsService implements UserDetailsService {

	public static final String NAME = "applicationUserDetailsService";

	@Autowired
	private BaseRepository dao;

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Account account = dao.findById(username, Account.class);

		return new ApplicationUserDetails(username, account.getPassword(),
				Set.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())), account.getRole());
	}

}
