package adn.dao.generic;

import java.util.function.Function;

import org.springframework.transaction.annotation.Transactional;

import adn.application.context.ContextProvider;
import adn.model.entities.Entity;
import io.jsonwebtoken.lang.Assert;

public class EntityGeneBuilder<T extends Entity> implements GeneBuilder<T> {

	private GenericDAO<T> dao;

	private Strategy<T> procedure;

	@SuppressWarnings("unchecked")
	public EntityGeneBuilder(Class<? extends T> clazz) {
		super();
		// TODO Auto-generated constructor stub
		Assert.notNull(clazz, "Entity class can not be null");
		this.dao = (GenericDAO<T>) ContextProvider.getApplicationContext().getBean(GenericDAOProvider.class)
				.getService(clazz);
		this.procedure = new Strategy<T>(dao::defaultBuild);
	}

	public EntityGeneBuilder<T> insertion() {
		this.procedure.and(new Strategy<T>(dao::insertionBuild));

		return this;
	}

	public EntityGeneBuilder<T> update() {
		this.procedure.and(new Strategy<T>(dao::updateBuild));

		return this;
	}

	public EntityGeneBuilder<T> deactivation() {
		this.procedure.and(new Strategy<T>(dao::deactivationBuild));

		return this;
	}

	public EntityGeneBuilder<T> then(Function<T, T> procedure) {
		this.procedure.and(new Strategy<T>(procedure));

		return this;
	}

	@Override
	public T build(T instance) {
		try {
			return this.procedure.execute(instance);
		} catch (RuntimeException e) {
			e.printStackTrace();

			return instance;
		}
	}

}

class Strategy<T extends Entity> {

	Strategy<T> next;

	Function<T, T> procedure;

	public Strategy(Function<T, T> procedure) {
		this.procedure = procedure;
	}

	public void and(Strategy<T> next) {
		if (this.next == null) {
			this.next = next;

			return;
		}

		this.next.and(next);
	}

	@Transactional
	public T execute(T instance) {
		if (this.next == null) {
			return this.procedure.apply(instance);
		}

		return this.next.execute(this.procedure.apply(instance));
	}

}
