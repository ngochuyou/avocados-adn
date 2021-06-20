package adn.service.entity;

import java.util.function.Function;

import org.springframework.transaction.annotation.Transactional;

import adn.application.context.ContextProvider;
import adn.dao.EntityBuilder;
import adn.model.entities.Entity;

public class CompositeGeneBuilder<T extends Entity> implements GeneBuilder<T> {

	private EntityBuilder<T> entityBuilder;

	private Strategy<T> procedure;

	public CompositeGeneBuilder(Class<T> clazz) {
		super();
		this.entityBuilder = ContextProvider.getApplicationContext().getBean(EntityBuilderProvider.class)
				.getBuilder(clazz);
		this.procedure = new Strategy<T>(entityBuilder::defaultBuild);
	}

	@Override
	public GeneBuilder<T> insertion() {
		this.procedure.and(new Strategy<T>(entityBuilder::insertionBuild));

		return this;
	}

	@Override
	public GeneBuilder<T> update() {
		this.procedure.and(new Strategy<T>(entityBuilder::updateBuild));

		return this;
	}

	@Override
	public GeneBuilder<T> deactivation() {
		this.procedure.and(new Strategy<T>(entityBuilder::deactivationBuild));

		return this;
	}

	public CompositeGeneBuilder<T> then(Function<T, T> procedure) {
		this.procedure.and(new Strategy<T>(procedure));

		return this;
	}

	@Override
	public T build(T instance) {
		return this.procedure.execute(instance);
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
	public T execute(T instance) throws RuntimeException {
		try {
			if (this.next == null) {
				return this.procedure.apply(instance);
			}

			return this.next.execute(this.procedure.apply(instance));
		} catch (Exception any) {
			throw new RuntimeException(any);
		}
	}

}
