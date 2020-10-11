package adn.service.transaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodBasedAction implements Action {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Object invoker;

	private Method method;

	private Object[] values;

	private Object output;

	public MethodBasedAction(Object invoker, Method method, Object[] values, Object output) {
		super();
		this.invoker = invoker;
		this.method = method;
		this.values = values;
		this.output = output;
	}

	public Object getInvoker() {
		return invoker;
	}

	public void setInvoker(Object invoker) {
		this.invoker = invoker;
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

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}

	@Override
	public void execute() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		logger.debug("Invoking " + this.method.getName() + " from " + this.invoker + " with values: "
				+ Stream.of(this.values).map(v -> v.toString()).collect(Collectors.joining(", ")));
		
		this.method.invoke(this.invoker, this.values);
	}

}
