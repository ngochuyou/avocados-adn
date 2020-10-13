package adn.service;

import adn.service.ADNService.ServiceStatus;

public class ServiceResult<T> {

	protected ServiceStatus status;

	protected T body;
	
	public ServiceResult(ServiceStatus status, T body) {
		super();
		this.status = status;
		this.body = body;
	}

	public ServiceStatus getStatus() {
		return status;
	}

	public void setStatus(ServiceStatus status) {
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
		return this.status.equals(ServiceStatus.OK);
	}
	
	public static <T> ServiceResult<T> status(ServiceStatus status, Class<T> clazz) {
		return new ServiceResult<>(status, null);
	}
	
	public static ServiceResult<String> bad() {
		return new ServiceResult<>(ServiceStatus.BAD, "BAD INVOKE");
	}
	
	public static <T> ServiceResult<T> ok(T body) {
		return new ServiceResult<>(ServiceStatus.OK, body);
	}
	
}
