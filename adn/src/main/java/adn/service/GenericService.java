/**
 * 
 */
package adn.service;

import adn.model.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericService<T extends Entity> {

	default T doProcedure(T model) {

		return model;
	};

	default T doInsertionProcedure(T model) {
		return model;
	};

	default T doUpdateProcedure(T model) {
		return model;
	};

	default T doDeactivationProcedure(T model) {
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
	public T doProcedure(T model) {
		// TODO Auto-generated method stub
		return right.doProcedure(left.doProcedure(model));
	}

	@Override
	public T doInsertionProcedure(T model) {
		// TODO Auto-generated method stub
		return right.doInsertionProcedure(left.doInsertionProcedure(model));
	}

	@Override
	public T doDeactivationProcedure(T model) {
		// TODO Auto-generated method stub
		return right.doDeactivationProcedure(left.doDeactivationProcedure(model));
	}

	@Override
	public T doUpdateProcedure(T model) {
		// TODO Auto-generated method stub
		return right.doUpdateProcedure(left.doUpdateProcedure(model));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + left.getName() + ", " + right.getName() + ']';
	}

}