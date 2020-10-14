/**
 * 
 */
package adn.service.context.transaction;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ngoc Huy
 *
 */
public class MethodEvent<T> implements Event<T> {

	private Method method;

	private Object invoker;

	private Object[] params;

	private T output;

	private Class<T> clazz;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public MethodEvent(Method method, Object invoker, Class<T> outputType, Object... params) {
		super();
		this.method = method;
		this.invoker = invoker;
		this.params = params;
		this.clazz = outputType;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object getInvoker() {
		return invoker;
	}

	public void setInvoker(Object invoker) {
		this.invoker = invoker;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public T getOutput() {
		return output;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public void setClazz(Class<T> clazz) {
		this.clazz = clazz;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		Object o = method.invoke(invoker, params);

		logger.debug("Invoking MethodEvent. Method name: " + this.method.getName());

		this.output = (T) o;
	}

}
