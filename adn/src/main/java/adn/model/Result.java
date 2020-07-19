/**
 * 
 */
package adn.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public class Result<T extends AbstractModel> {

	protected Set<Integer> status;

	protected T instance;

	protected Map<String, String> messageSet;

	public static final int OK = 200;

	public static final int CONFLICT = 409;

	public static final int BAD = 400;

	public static final int FAILED = 500;

	public static final int NULL = -400;
	
	public Result(Set<Integer> status, T instance, Map<String, String> messageSet) {
		super();
		this.status = status;
		this.instance = instance;
		this.messageSet = messageSet;
	}

	public Set<Integer> getStatus() {
		return status;
	}

	public void setStatus(Set<Integer> status) {
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

		return this.status.stream().filter(status -> status != 200).count() == 0;
	}

	public static <T extends AbstractModel> Result<T> success(T instance) {

		return new Result<T>(Set.of(Result.OK), instance, new HashMap<>());
	}

	public static <T extends AbstractModel> Result<T> error(Set<Integer> status, T instance,
			Map<String, String> messageSet) {

		return new Result<T>(status, instance, messageSet);
	}

}
