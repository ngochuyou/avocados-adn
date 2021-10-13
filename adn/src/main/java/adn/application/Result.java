/**
 * 
 */
package adn.application;

import static adn.service.internal.Service.Status.BAD;
import static adn.service.internal.Service.Status.FAILED;
import static adn.service.internal.Service.Status.OK;

import java.util.HashMap;
import java.util.Map;

import adn.service.internal.Service.Status;

/**
 * @author Ngoc Huy
 *
 */
public class Result<T> {

	private Status status; // either OK or BAD for Validator operations
	private T instance;
	private Map<String, String> messages;

	public Result(T instance) {
		status = OK;
		this.instance = instance;
		messages = new HashMap<>();
	}

	public Result(Status status, T instance, Map<String, String> messageSet) {
		super();
		this.status = status;
		this.instance = instance;
		this.messages = messageSet;
	}

	public Result<T> success() {
		this.status = OK;
		return this;
	}

	public Status getStatus() {
		return status;
	}

	public Result<T> setStatus(Status status) {
		this.status = status;
		return this;
	}

	public T getInstance() {
		return instance;
	}

	public Result<T> bad() {
		this.status = BAD;
		return this;
	}
	
	public Result<T> bad(String key, String message) {
		this.status = BAD;
		messages.put(key, message);
		return this;
	}

	public Result<T> setInstance(T instance) {
		this.instance = instance;
		return this;
	}

	public Map<String, String> getMessages() {
		return messages;
	}

	public boolean isOk() {
		return this.status == OK;
	}

	public static <T> Result<T> ok(T instance) {
		return new Result<T>(OK, instance, new HashMap<>());
	}

	public static <T> Result<T> bad(Map<String, String> messageSet) {
		return new Result<T>(BAD, null, messageSet);
	}
	
	public static <T> Result<T> bad(String message) {
		return bad(Map.of(Common.MESSAGE, message));
	}

	public static <T> Result<T> failed(String error) {
		return new Result<T>(FAILED, null, Map.of(Common.ERROR, error));
	}

}
