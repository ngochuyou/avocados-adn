package adn.service;

import adn.service.ApplicationService.ServiceStatus;

public class ServiceResult {

	protected ServiceStatus status;

	protected Object body;
	
	public ServiceResult(ServiceStatus status, Object body) {
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

	public Object getBody() {
		return body;
	}

	public ServiceResult setBody(Object body) {
		this.body = body;
		
		return this;
	}
	
	public boolean isOk() {
		
		return this.status == ServiceStatus.OK;
	}
	
	public static ServiceResult status(ServiceStatus status) {
		return new ServiceResult(status, null);
	}
	
	public static ServiceResult bad() {
		return new ServiceResult(ServiceStatus.BAD, "BAD INVOKE");
	}
	
	public static ServiceResult ok(Object body) {
		return new ServiceResult(ServiceStatus.BAD, body);
	}
	
}
