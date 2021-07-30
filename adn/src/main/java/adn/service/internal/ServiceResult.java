package adn.service.internal;

import adn.service.internal.Service.Status;

public class ServiceResult<T> {

	protected Status status;

	protected T body;

	public ServiceResult(Status status) {
		super();
		this.status = status;
	}

	public ServiceResult(Status status, T body) {
		super();
		this.status = status;
		this.body = body;
	}

	public Status getStatus() {
		return status;
	}

	public ServiceResult<T> setStatus(Status status) {
		this.status = status;
		return this;
	}

	public T getBody() {
		return body;
	}

	public ServiceResult<T> body(T body) {
		this.body = body;

		return this;
	}

	public boolean isOk() {
		return this.status.equals(Status.OK);
	}

	public static <T> ServiceResult<T> status(Status status) {
		return new ServiceResult<>(status);
	}

	public static <T> ServiceResult<T> bad() {
		return new ServiceResult<>(Status.BAD);
	}

	public static <T> ServiceResult<T> ok(T body) {
		return new ServiceResult<>(Status.OK, body);
	}

}
