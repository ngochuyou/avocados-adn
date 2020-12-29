package adn.service;

import adn.service.ADNService.Status;

public class ServiceResult<T> {

	protected Status status;

	protected T body;
	
	public ServiceResult(Status status, T body) {
		super();
		this.status = status;
		this.body = body;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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
	
	public static <T> ServiceResult<T> status(Status status, Class<T> clazz) {
		return new ServiceResult<>(status, null);
	}
	
	public static ServiceResult<String> bad() {
		return new ServiceResult<>(Status.BAD, "BAD INVOKE");
	}
	
	public static <T> ServiceResult<T> ok(T body) {
		return new ServiceResult<>(Status.OK, body);
	}
	
}
