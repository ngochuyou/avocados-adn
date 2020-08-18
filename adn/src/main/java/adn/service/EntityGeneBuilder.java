package adn.service;

import java.util.function.Function;

import adn.application.ApplicationContextProvider;
import adn.application.managers.GenericServiceProvider;
import adn.model.entities.Entity;

public class EntityGeneBuilder<T extends Entity> {

	private GenericService<T> service;

	private Strategy<T> procedure;

	public EntityGeneBuilder(Class<T> clazz) {
		super();
		// TODO Auto-generated constructor stub
		this.service = ApplicationContextProvider.getApplicationContext().getBean(GenericServiceProvider.class)
				.getService(clazz);
		this.procedure = new Strategy<T>(service::executeDefaultProcedure);
	}

	public EntityGeneBuilder<T> insert() {
		this.procedure.and(new Strategy<T>(service::executeInsertionProcedure));

		return this;
	}

	public EntityGeneBuilder<T> update() {
		this.procedure.and(new Strategy<T>(service::executeUpdateProcedure));

		return this;
	}

	public EntityGeneBuilder<T> deactivate() {
		this.procedure.and(new Strategy<T>(service::executeDeactivationProcedure));

		return this;
	}

	public EntityGeneBuilder<T> then(Function<T, T> procedure) {
		this.procedure.and(new Strategy<T>(procedure));

		return this;
	}

	public T build(T instance) {

		return this.procedure.execute(instance);
	}
}

class Strategy<T extends Entity> {

	Strategy<T> next;

	Function<T, T> procedure;

	public Strategy(Function<T, T> procedure) {
		super();
		this.procedure = procedure;
	}

	public void and(Strategy<T> next) {
		if (this.next == null) {
			this.next = next;

			return;
		}

		this.next.and(next);
	}

	public T execute(T instance) {
		if (this.next == null) {
			return this.procedure.apply(instance);
		}

		return this.next.execute(this.procedure.apply(instance));
	}

}
