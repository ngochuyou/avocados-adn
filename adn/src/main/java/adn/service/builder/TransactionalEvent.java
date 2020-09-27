package adn.service.builder;

import java.lang.reflect.Method;

public class TransactionalEvent {

	protected Object invoker;

	protected Method method;

	protected Object[] values;

	public TransactionalEvent(Object invoker, Method method, Object[] values) {
		super();
		this.invoker = invoker;
		this.method = method;
		this.values = values;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object[] getValues() {
		return values;
	}

	public void setValues(Object[] values) {
		this.values = values;
	}

	public Object getInvoker() {
		return invoker;
	}

	public void setInvoker(Object invoker) {
		this.invoker = invoker;
	}

}
