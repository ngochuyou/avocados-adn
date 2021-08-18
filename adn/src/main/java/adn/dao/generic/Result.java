/**
 * 
 */
package adn.dao.generic;

import static adn.service.internal.Service.Status.BAD;
import static adn.service.internal.Service.Status.FAILED;
import static adn.service.internal.Service.Status.OK;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import adn.service.internal.Service.Status;

/**
 * @author Ngoc Huy
 *
 */
public class Result<T> {

	protected Status status; // either OK or FAILED
	protected T instance;
	protected Map<String, String> messages;

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

	public static <T> Result<T> success(T instance) {
		return new Result<T>(OK, instance, new HashMap<>());
	}

	public static <T> Result<T> bad(Map<String, String> messageSet) {
		return new Result<T>(BAD, null, messageSet);
	}

	public static <T> Result<T> failed(String error) {
		return new Result<T>(FAILED, null, Map.of("exception", error));
	}

	public static <T> ResultReducer<T> with(Result<T> result) {
		return new ResultReducer<>(result);
	}

	public static class ResultReducer<T> {

		private final Result<T> result;

		private ResultReducer(Result<T> result) {
			this.result = result;
		}

		public Result<T> make(Consumer<Result<T>> reducer) {
			reducer.accept(result);

			return result;
		}

	}

}
