/**
 * 
 */
package adn.security.context;

import adn.security.UserDetailsImpl;

/**
 * @author Ngoc Huy
 *
 */
public class Mutex {

	private volatile UserDetailsImpl userInfo;

	public Mutex(UserDetailsImpl userInfo) {
		super();
		this.userInfo = userInfo;
	}

	public UserDetailsImpl getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserDetailsImpl userInfo) {
		this.userInfo = userInfo;
	}

}
