/**
 * 
 */
package adn.security;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import adn.dao.BaseDAO;
import adn.model.entities.Account;

/**
 * @author Ngoc Huy
 *
 */
@Service
public class ApplicationUserDetailsService implements UserDetailsService {

	@Autowired
	private BaseDAO dao;

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		Account account = dao.findById(username, Account.class);

		return new User(username, account.getPassword(),
				Set.of(new SimpleGrantedAuthority("ROLE_" + account.getRole())));
	}

}
