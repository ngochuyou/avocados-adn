package adn.service;

import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adn.application.managers.GenericServiceProvider;
import adn.model.Entity;

@Component
public class GenericServiceExecutor implements ServiceExecutor {

	@Autowired
	protected GenericServiceProvider serviceProvider;

	protected Map<GenericStrategy, String> methodNameMap = Map.of(GenericStrategy.DEFAULT, "doProcedure",
			GenericStrategy.INSERT, "doInsertionProcedure", GenericStrategy.UPDATE, "doUpdateProcedure",
			GenericStrategy.DEACTIVATE, "doDeactivationProcedure");

	@Override
	public <T extends Entity> T execute(T instance, Class<T> clazz) {
		// TODO Auto-generated method stub
		return serviceProvider.getService(clazz).doProcedure(instance);
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> T execute(T instance, GenericStrategy... strategies) throws Exception {
		Class<T> clazz = (Class<T>) instance.getClass();
		GenericService<T> service = serviceProvider.getService(clazz);
		GenericServiceStrategyChain<T> chain = new GenericServiceStrategyChain<T>(
				service.getClass().getDeclaredMethod(methodNameMap.get(strategies[0]), Entity.class));

		for (int i = 1; i < strategies.length; i++) {
			chain.register(new GenericServiceStrategyChain<T>(
					service.getClass().getDeclaredMethod(methodNameMap.get(strategies[i]), Entity.class)));
		}

		return (T) chain.execute(service, instance);
	}

	public <T extends Entity> T execute(T instance) throws Exception {

		return this.execute(instance, GenericStrategy.DEFAULT);
	}

}

class GenericServiceStrategyChain<T extends Entity> {

	private Method strategy;

	private GenericServiceStrategyChain<?> nextChain;

	public GenericServiceStrategyChain(Method strategy) {
		super();
		this.strategy = strategy;
	}

	public void register(GenericServiceStrategyChain<?> nextChain) {
		if (this.nextChain == null) {
			this.nextChain = nextChain;
			
			return;
		}

		this.nextChain.register(nextChain);
	}

	@SuppressWarnings("unchecked")
	public Object execute(GenericService<?> service, Object entity) throws Exception {
		if (this.nextChain == null) {
			return (T) this.strategy.invoke(service, entity);
		}

		return (T) this.strategy.invoke(service, this.nextChain.execute(service, entity));
	}

}
