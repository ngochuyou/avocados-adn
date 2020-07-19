/**
 * 
 */
package adn.model.specification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import adn.model.Entity;
import adn.model.Result;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T extends Entity> {

	default Result<T> isSatisfiedBy(T instance) {

		return Result.success(instance);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	default Specification<?> and(Specification<?> next) {

		return new And(this, next);
	}

	default String getName() {

		return this.getClass().getSimpleName().length() == 0 ? this.getClass().getName()
				: this.getClass().getSimpleName();
	}

}

class And<T extends Entity> implements Specification<T> {

	private Specification<T> left;

	private Specification<T> right;

	public And(Specification<T> left, Specification<T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public Result<T> isSatisfiedBy(T instance) {
		// TODO Auto-generated method stub
		Result<T> left = this.left.isSatisfiedBy(instance);
		Result<T> right = this.right.isSatisfiedBy(instance);

		if (left.isOk() && right.isOk()) {

			return Result.success(instance);
		}

		Map<String, String> messageSet = new HashMap<>();
		Set<Integer> status = left.getStatus();

		messageSet.putAll(left.getMessageSet());
		messageSet.putAll(right.getMessageSet());
		status.addAll(right.getStatus());

		return Result.error(status, instance, messageSet);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "[" + this.left.getName() + ", " + this.right.getName() + "]";
	}

}
