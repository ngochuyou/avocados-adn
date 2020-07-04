/**
 * 
 */
package adn.model.specification;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import adn.model.Model;
import adn.model.ModelResult;

/**
 * @author Ngoc Huy
 *
 */
public abstract class CompositeSpecification<T extends Model> implements Specification<T> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Specification<?> and(Specification<?> next) {
		// TODO Auto-generated method stub
		return new And(this, next);
	}

}

class And<T extends Model> extends CompositeSpecification<T> {

	private Specification<T> left;

	private Specification<T> right;

	public And(Specification<T> left, Specification<T> right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	public ModelResult<T> isSatisfiedBy(T instance) {
		// TODO Auto-generated method stub
		ModelResult<T> left = this.left.isSatisfiedBy(instance);
		ModelResult<T> right = this.right.isSatisfiedBy(instance);

		if (left.isOk() && right.isOk()) {

			return ModelResult.success(instance);
		}

		Map<String, String> messageSet = new HashMap<>();
		Set<Integer> status = left.getStatus();

		messageSet.putAll(left.getMessageSet());
		messageSet.putAll(right.getMessageSet());
		status.addAll(right.getStatus());

		return ModelResult.error(status, instance, messageSet);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "[" + this.left.getName() + ", " + this.right.getName() + "]";
	}

}
