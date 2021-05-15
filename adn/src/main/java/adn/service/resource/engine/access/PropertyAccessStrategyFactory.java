/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.helpers.FunctionHelper.HandledFunction;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("rawtypes")
public class PropertyAccessStrategyFactory {

	private PropertyAccessStrategyFactory() {}

	public static final PropertyAccessStrategy NO_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		private final PropertyAccess NO_ACCESS = new NoAccess();

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return NO_ACCESS;
		}

	};

	public static final PropertyAccessStrategy DIRECT_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new DirectAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategy STANDARD_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new StandardAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategy LITERALLY_NAMED_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new LiterallyNamedAccess(containerJavaType, propertyName, Object.class);
		}

	};

	public static final PropertyAccessStrategy DELEGATE_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			try {
				return new StandardAccess(containerJavaType, propertyName);
			} catch (IllegalArgumentException iae) {
				return new PropertyAccessDelegateImpl(locateGetter(containerJavaType, propertyName),
						locateSetter(containerJavaType, propertyName));
			}
		}

	};

	public static Getter locateGetter(Class containerJavaType, String propertyName) {
		Getter getter = StandardAccess.locateGetter(containerJavaType, propertyName).orElse(null);

		if (getter != null) {
			return getter;
		}

		getter = LiterallyNamedAccess.locateGetter(containerJavaType, propertyName).orElse(null);

		if (getter != null) {
			return getter;
		}

		return DirectAccess.locateGetter(containerJavaType, propertyName).orElse(NoAccess.NO_OP_GETTER);
	}

	public static Setter locateSetter(Class containerJavaType, String propertyName) {
		Setter setter = StandardAccess.locateSetter(containerJavaType, propertyName).orElse(null);

		if (setter != null) {
			return setter;
		}

		setter = LiterallyNamedAccess.locateSetter(containerJavaType, propertyName, Object.class).orElse(null);

		if (setter != null) {
			return setter;
		}

		return DirectAccess.locateSetter(containerJavaType, propertyName).orElse(NoAccess.NO_OP_SETTER);
	}

	public static final SpecificPropertyAccessStrategy SPECIFIC_ACCESS_STRATEGY = new SpecificPropertyAccessStrategy() {

		@Override
		public PropertyAccess buildPropertyAccess(Getter getter, Setter setter) {
			return new PropertyAccessDelegateImpl(getter, setter).setPropertyAccessStrategy(this);
		}

	};

	public static final FunctionPropertyAccessStrategy FUNCTIONAL_ACCESS_STRATEGY = new FunctionPropertyAccessStrategy() {

		@Override
		public <T, R, E extends Throwable> PropertyAccess buildPropertyAccess(HandledFunction<T, R, E> getter,
				HandledFunction<T, R, E> setter) {
			return new FunctionalPropertyAccessImpl<>(getter, setter);
		}

	};

	public static final HybridPropertyAccessStrategy HYBRID_ACCESS_STRATEGY = new HybridPropertyAccessStrategy() {

		@Override
		public <T, R, E extends Throwable> PropertyAccess buildPropertyAccess(Getter getter, Setter setter,
				HandledFunction<T, R, E> getterFnc, HandledFunction<T, R, E> setterFnc) {
			return new FunctionalPropertyAccessImpl<>(getterFnc, setterFnc).setGetter(getter).setSetter(setter)
					.setPropertyAccessStrategy(this);
		}
	};

	interface DelegateDefaultAccessStrategy extends PropertyAccessStrategy {

		@Override
		default PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return DELEGATE_ACCESS_STRATEGY.buildPropertyAccess(containerJavaType, propertyName);
		}

	}

	public interface SpecificPropertyAccessStrategy extends PropertyAccessStrategy, DelegateDefaultAccessStrategy {

		PropertyAccess buildPropertyAccess(Getter getter, Setter setter);

	}

	public interface FunctionPropertyAccessStrategy extends PropertyAccessStrategy, DelegateDefaultAccessStrategy {

		<T, R, E extends Throwable> PropertyAccess buildPropertyAccess(HandledFunction<T, R, E> getter,
				HandledFunction<T, R, E> setter);

	}

	public interface HybridPropertyAccessStrategy extends PropertyAccessStrategy, DelegateDefaultAccessStrategy {

		<T, R, E extends Throwable> PropertyAccess buildPropertyAccess(Getter getter, Setter setter,
				HandledFunction<T, R, E> getterFnc, HandledFunction<T, R, E> setterFnc);

	}

	public interface PropertyAccessBuilder extends PropertyAccess {

		PropertyAccessBuilder setGetter(Getter getter);

		PropertyAccessBuilder setSetter(Setter setter);

		PropertyAccessBuilder setPropertyAccessStrategy(PropertyAccessStrategy strategy);

	}

	public interface PropertyAccessDelegate extends PropertyAccessBuilder {

		default boolean hasGetter() {
			return getGetter() != null && getGetter() != NoAccess.NO_OP_GETTER;
		}

		default boolean hasSetter() {
			return getSetter() != null && getSetter() != NoAccess.NO_OP_SETTER;
		}

	}

	public interface FunctionalPropertyAccess<T, R, E extends Throwable> extends PropertyAccessBuilder {

		HandledFunction<T, R, E> getGetterFunction();

		HandledFunction<T, R, E> getSetterFunction();

		public static final HandledFunction NO_OP_FNC = new HandledFunction<>() {
			@Override
			public Object apply(Object arg) throws Throwable {
				return null;
			}
		};

		FunctionalPropertyAccess<T, R, E> setGetterFunction(HandledFunction<T, R, E> getter);

		FunctionalPropertyAccess<T, R, E> setSetterFunction(HandledFunction<T, R, E> setter);

	}

}
