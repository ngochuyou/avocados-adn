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

	public void setBody(Object body) {
		this.body = body;
	}

}
