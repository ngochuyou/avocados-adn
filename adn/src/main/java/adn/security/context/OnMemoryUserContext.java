/**
 * 
 */
package adn.security.context;

import java.util.LinkedHashMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.model.entities.User;
import adn.security.UserDetailsImpl;
import adn.service.DomainEntityServiceObserver;
import adn.service.services.AccountService;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class OnMemoryUserContext implements DomainEntityServiceObserver<User> {

	private static final Logger logger = LoggerFactory.getLogger(OnMemoryUserContext.class);
	private static final String ID = UUID.randomUUID().toString();

	private final MutexMap context = new MutexMap();

	@Autowired
	public OnMemoryUserContext(AccountService service) {
		service.register(this);
	}

	public UserDetailsImpl getUser(String username) {
		Mutex mutex;

		if ((mutex = context.get(username)) == null) {
			return null;
		}

		return mutex.getUserInfo();
	}

	public void put(UserDetailsImpl userInfo) {
		Mutex mutex;

		if ((mutex = context.get(userInfo.getUsername())) != null) {
			synchronized (mutex) {
				mutex.setUserInfo(userInfo);
				return;
			}
		}

		mutex = new Mutex(userInfo);

		synchronized (mutex) {
			context.put(userInfo.getUsername(), mutex);
		}

		return;
	}

	public void remove(String username) {
		Mutex mutex;

		if ((mutex = context.get(username)) != null) {
			synchronized (mutex) {
				context.remove(username);
			}

			return;
		}
	}

	@Override
	public void doNotify() throws Exception {}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void notifyUpdate(User newState) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Removing user info on user update, user id: [%s]", newState.getId()));
		}

		remove(newState.getId());
	}

	@Override
	public void notifyCreation(User newState) {}

}

class MutexMap extends LinkedHashMap<String, Mutex> {

	private static final long serialVersionUID = 1L;

	private static final int MAX_SIZE = 100;

	public MutexMap() {
		super(MAX_SIZE, 1.05f);
	}

	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<String, Mutex> eldest) {
		return size() >= MAX_SIZE;
	}

}