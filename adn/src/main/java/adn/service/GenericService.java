/**
 * 
 */
package adn.service;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericService<T extends Entity> extends ApplicationService {

	default T executeDefaultProcedure(T model) {
		return model;
	};

	default T executeInsertionProcedure(T model) {
		return model;
	};

	default T executeUpdateProcedure(T model) {
		return model;
	};

	default T executeDeactivationProcedure(T model) {
		return model;
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default GenericService<?> and(GenericService<?> next) {

		return new And(this, next);
	}

	default String getName() {

		return this.getClass().getSimpleName();
	}

}

class And<T extends Entity> implements GenericService<T> {

	private GenericService<T> left;

	private GenericService<T> right;

	public And(GenericService<T> left, GenericService<T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	public GenericService<T> getLeft() {
		return left;
	}

	public void setLeft(GenericService<T> left) {
		this.left = left;
	}

	public GenericService<T> getRight() {
		return right;
	}

	public void setRight(GenericService<T> right) {
		this.right = right;
	}

	@Override
	public T executeDefaultProcedure(T model) {
		// TODO Auto-generated method stub
		return right.executeDefaultProcedure(left.executeDefaultProcedure(model));
	}

	@Override
	public T executeInsertionProcedure(T model) {
		// TODO Auto-generated method stub
		return right.executeInsertionProcedure(left.executeInsertionProcedure(model));
	}

	@Override
	public T executeDeactivationProcedure(T model) {
		// TODO Auto-generated method stub
		return right.executeDeactivationProcedure(left.executeDeactivationProcedure(model));
	}

	@Override
	public T executeUpdateProcedure(T model) {
		// TODO Auto-generated method stub
		return right.executeUpdateProcedure(left.executeUpdateProcedure(model));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + left.getName() + ", " + right.getName() + ']';
	}

}