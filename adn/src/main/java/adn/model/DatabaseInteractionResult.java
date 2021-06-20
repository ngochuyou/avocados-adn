/**
 * 
 */
package adn.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;

/**
 * @author Ngoc Huy
 *
 */
public class DatabaseInteractionResult<T extends AbstractModel> {

	protected short status;

	protected T instance;

	protected Map<String, String> messages;

	public DatabaseInteractionResult(T instance) {
		status = (short) HttpStatus.OK.value();
		this.instance = instance;
		messages = new HashMap<>();
	}

	public DatabaseInteractionResult(int status, T instance, Map<String, String> messageSet) {
		super();
		this.status = (short) status;
		this.instance = instance;
		this.messages = messageSet;
	}

	public DatabaseInteractionResult<T> success() {
		this.status = (short) HttpStatus.OK.value();
		return this;
	}

	public short getStatus() {
		return status;
	}

	public DatabaseInteractionResult<T> setStatus(int status) {
		this.status = (short) status;
		return this;
	}

	public T getInstance() {
		return instance;
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

	public static <T extends AbstractModel> DatabaseInteractionResult<T> success(T instance) {
		return new DatabaseInteractionResult<T>((short) HttpStatus.OK.value(), instance, new HashMap<>());
	}

	public static <T extends AbstractModel> DatabaseInteractionResult<T> error(short status, T instance,
			Map<String, String> messageSet) {
		return new DatabaseInteractionResult<T>(status, instance, messageSet);
	}

}
