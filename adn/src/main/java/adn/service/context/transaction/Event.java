/**
 * 
 */
package adn.service.context.transaction;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public interface Event<T> {

	void execute() throws Exception;

	static <P, R> Event<R> functional(Function<P, R> function, P param, String functionName) {

		return new FunctionalEvent<P, R>(functionName, function, param);
	};

	static <X, Y, R> Event<R> biFunctional(BiFunction<X, Y, R> function, X param1, Y param2, String functionName) {

		return new BiFunctionalEvent<X, Y, R>(functionName, function, param1, param2);
	};

}

class FunctionalEvent<T, R> implements Event<R> {

	private String functionName;

	private Function<T, R> function;

	private T param;

	private R output;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	protected FunctionalEvent(String functionName, Function<T, R> function, T param) {
		super();
		this.functionName = functionName;
		Assert.notNull(function, "Function cannot be null");
		Assert.notNull(param, "Param cannot be null");
		this.function = function;
		this.param = param;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Function<T, R> getFunction() {
		return function;
	}

	public void setFunction(Function<T, R> function) {
		this.function = function;
	}

	public T getParam() {
		return param;
	}

	public void setParam(T param) {
		this.param = param;
	}

	public R getOutput() {
		return output;
	}

	public void setOutput(R output) {
		this.output = output;
	}

	@Override
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		logger.debug("Invoking Function lambda: " + this.functionName);
		this.output = this.function.apply(this.param);
	}

}

class BiFunctionalEvent<X, Y, R> implements Event<R> {

	private String functionName;

	private BiFunction<X, Y, R> function;

	private X fisrtParam;

	private Y secondParam;

	private R output;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public BiFunctionalEvent(String functionName, BiFunction<X, Y, R> function, X fisrtParam, Y secondParam) {
		super();
		this.functionName = functionName;
		this.function = function;
		this.fisrtParam = fisrtParam;
		this.secondParam = secondParam;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public BiFunction<X, Y, R> getFunction() {
		return function;
	}

	public void setFunction(BiFunction<X, Y, R> function) {
		this.function = function;
	}

	public X getFisrtParam() {
		return fisrtParam;
	}

	public void setFisrtParam(X fisrtParam) {
		this.fisrtParam = fisrtParam;
	}

	public Y getSecondParam() {
		return secondParam;
	}

	public void setSecondParam(Y secondParam) {
		this.secondParam = secondParam;
	}

	public R getOutput() {
		return output;
	}

	public void setOutput(R output) {
		this.output = output;
	}

	@Override
	public void execute() throws Exception {
		// TODO Auto-generated method stub
		logger.debug("Invoking BiFunction lambda: " + this.functionName);
		output = this.function.apply(fisrtParam, secondParam);
	}

}
