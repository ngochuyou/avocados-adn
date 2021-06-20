package adn.service;

import adn.service.Service.Status;

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
	
	public static <T> ServiceResult<T> status(Status status) {
		return new ServiceResult<>(status);
	}
	
	public static ServiceResult<String> bad() {
		return new ServiceResult<>(Status.BAD);
	}
	
	public static <T> ServiceResult<T> ok(T body) {
		return new ServiceResult<>(Status.OK, body);
	}
	
}
