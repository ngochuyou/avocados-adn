/**
 * 
 */
package adn.dao;

import adn.model.entities.Entity;

/**
 * @author Ngoc Huy
 *
 */
public interface GenericDAO<T extends Entity> {

	default T defaultBuild(T model) {
		return model;
	};

	default T insertBuild(T model) {
		return model;
	};

	default T updateBuild(T model) {
		return model;
	};

	default T deactivationBuild(T model) {
		return model;
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	default GenericDAO<?> and(GenericDAO<?> next) {

		return new And(this, next);
	}

	default String getName() {

		return this.getClass().getSimpleName();
	}

}

class And<T extends Entity> implements GenericDAO<T> {

	private GenericDAO<T> left;

	private GenericDAO<T> right;

	public And(GenericDAO<T> left, GenericDAO<T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	public GenericDAO<T> getLeft() {
		return left;
	}

	public void setLeft(GenericDAO<T> left) {
		this.left = left;
	}

	public GenericDAO<T> getRight() {
		return right;
	}

	public void setRight(GenericDAO<T> right) {
		this.right = right;
	}

	@Override
	public T defaultBuild(T model) {
		// TODO Auto-generated method stub
		return right.defaultBuild(left.defaultBuild(model));
	}

	@Override
	public T insertBuild(T model) {
		// TODO Auto-generated method stub
		return right.insertBuild(left.insertBuild(model));
	}

	@Override
	public T deactivationBuild(T model) {
		// TODO Auto-generated method stub
		return right.deactivationBuild(left.deactivationBuild(model));
	}

	@Override
	public T updateBuild(T model) {
		// TODO Auto-generated method stub
		return right.updateBuild(left.updateBuild(model));
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return '[' + left.getName() + ", " + right.getName() + ']';
	}

}