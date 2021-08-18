/**
 * 
 */
package adn.security.context;

import adn.security.ApplicationUserDetails;

/**
 * @author Ngoc Huy
 *
 */
public class Mutex {

	private volatile ApplicationUserDetails userInfo;

	public Mutex(ApplicationUserDetails userInfo) {
		super();
		this.userInfo = userInfo;
	}

	public ApplicationUserDetails getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(ApplicationUserDetails userInfo) {
		this.userInfo = userInfo;
	}

}
