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
public class Result<T extends AbstractModel> {

	protected int status;

	protected T instance;

	protected Map<String, String> messageSet;

	public Result(int status, T instance, Map<String, String> messageSet) {
		super();
		this.status = status;
		this.instance = instance;
		this.messageSet = messageSet;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public T getInstance() {
		return instance;
	}

	public void setInstance(T instance) {
		this.instance = instance;
	}

	public Map<String, String> getMessageSet() {
		return messageSet;
	}

	public void setMessageSet(Map<String, String> messageSet) {
		this.messageSet = messageSet;
	}

	public boolean isOk() {

		return this.status == 200;
	}

	public static <T extends AbstractModel> Result<T> success(T instance) {

		return new Result<T>(HttpStatus.OK.value(), instance, new HashMap<>());
	}

	public static <T extends AbstractModel> Result<T> error(int status, T instance, Map<String, String> messageSet) {

		return new Result<T>(status, instance, messageSet);
	}

}
