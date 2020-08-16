package adn.model.factory.production.security;

import java.util.function.BiFunction;

import adn.model.Entity;
import adn.model.Model;
import adn.model.factory.ModelProducer;

public class CompositeModelProducer<M extends Model, E extends Entity>
		implements ModelProducer<M, E> {

	private BiFunction<M, E, M> strategy;

	private CompositeModelProducer<M, E> next;

	public CompositeModelProducer(BiFunction<M, E, M> strategy) {
		// TODO Auto-generated constructor stub
		this.strategy = strategy;
	}

	public void andThen(BiFunction<M, E, M> next) {
		if (this.next == null) {
			this.next = new CompositeModelProducer<>(next);

			return;
		}

		this.next.andThen(next);
	}

	@Override
	public M produce(E entity, M model) {
		// TODO Auto-generated method stub
		if (this.next == null) {
			return this.strategy.apply(model, entity);
		}

		return this.next.produce(entity, this.strategy.apply(model, entity));
	}

}
