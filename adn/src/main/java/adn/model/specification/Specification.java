/**
 * 
 */
package adn.model.specification;

import adn.model.Model;
import adn.model.ModelResult;

/**
 * @author Ngoc Huy
 *
 */
public interface Specification<T extends Model> {

	default ModelResult<T> isSatisfiedBy(T instance) {

		return ModelResult.success(instance);
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
