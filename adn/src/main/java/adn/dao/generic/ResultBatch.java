/**
 * 
 */
package adn.dao.generic;

import java.util.List;

import adn.service.internal.Service.Status;

/**
 * @author Ngoc Huy
 *
 */
public class ResultBatch<T> {

	private Status status;
	private List<Result<T>> results;
	private String message;

	public ResultBatch() {}

	public ResultBatch(Status status, List<Result<T>> results) {
		super();
		this.status = status;
		this.results = results;
	}

	public ResultBatch(Status status, List<Result<T>> results, String message) {
		super();
		this.status = status;
		this.results = results;
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<Result<T>> getResults() {
		return results;
	}

	public void setResults(List<Result<T>> results) {
		this.results = results;
	}

	public String getMessage() {
		return message;
	}

	public ResultBatch<T> setMessage(String message) {
		this.message = message;
		return this;
	}

	public boolean isOk() {
		return status == Status.OK;
	}

	public static <T> ResultBatch<T> ok(List<Result<T>> results) {
		return new ResultBatch<>(Status.OK, results);
	}

	public static <T> ResultBatch<T> failed(List<Result<T>> results) {
		return new ResultBatch<>(Status.FAILED, results);
	}

	public static <T> ResultBatch<T> bad(List<Result<T>> results) {
		return new ResultBatch<>(Status.BAD, results);
	}

}
