/**
 * 
 */
package adn.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * @author Ngoc Huy
 *
 */
public class DatabaseInteractionResult<T> {

	protected int status;

	protected T instance;

	protected Map<String, String> messages;

	public DatabaseInteractionResult(T instance) {
		status = HttpStatus.OK.value();
		this.instance = instance;
		messages = new HashMap<>();
	}

	public DatabaseInteractionResult(int status, T instance, Map<String, String> messageSet) {
		super();
		this.status = status;
		this.instance = instance;
		this.messages = messageSet;
	}

	public DatabaseInteractionResult<T> success() {
		this.status = HttpStatus.OK.value();
		return this;
	}

	public int getStatus() {
		return status;
	}

	public DatabaseInteractionResult<T> setStatus(int status) {
		this.status = status;
		return this;
	}

	public T getInstance() {
		return instance;
	}

	public DatabaseInteractionResult<T> bad() {
		this.status = HttpStatus.BAD_REQUEST.value();
		return this;
	}

	public DatabaseInteractionResult<T> setInstance(T instance) {
		this.instance = instance;
		return this;
	}

	public Map<String, String> getMessages() {
		return messages;
	}

	public boolean isOk() {
		return this.status == 200;
	}

	public static <T> DatabaseInteractionResult<T> success(T instance) {
		return new DatabaseInteractionResult<T>(HttpStatus.OK.value(), instance, new HashMap<>());
	}

	public static <T> DatabaseInteractionResult<T> failed(Map<String, String> messageSet) {
		return new DatabaseInteractionResult<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null, messageSet);
	}

	public static <T> DatabaseInteractionResult<T> unauthorized(T instance, Map<String, String> messageSet) {
		return new DatabaseInteractionResult<T>(HttpStatus.UNAUTHORIZED.value(), instance, messageSet);
	}

	public static <T> DatabaseInteractionResult<T> bad(T instance, Map<String, String> messageSet) {
		return new DatabaseInteractionResult<T>(HttpStatus.BAD_REQUEST.value(), instance, messageSet);
	}

	public static <T> DatabaseInteractionResult<T> error(int status, T instance, Map<String, String> messageSet) {
		return new DatabaseInteractionResult<T>(status, instance, messageSet);
	}

}
