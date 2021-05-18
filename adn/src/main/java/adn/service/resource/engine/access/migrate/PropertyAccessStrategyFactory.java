/**
 * 
 */
package adn.service.resource.engine.access.migrate;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("rawtypes")
public class PropertyAccessStrategyFactory {

	public static final PropertyAccessStrategyImplementor<NoAccess> NO_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<NoAccess>() {

		@Override
		public NoAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return NoAccess.INSTANCE;
		};

	};

	public static final PropertyAccessStrategyImplementor<StandardAccess> STANDARD_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<StandardAccess>() {

		@Override
		public StandardAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new StandardAccess(containerJavaType, propertyName);
		};

	};

	public static final PropertyAccessStrategyImplementor<DirectAccess> DIRECT_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<DirectAccess>() {

		@Override
		public DirectAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new DirectAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategyImplementor<LiterallyNamedAccess> LITERALLY_NAMED_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<LiterallyNamedAccess>() {

		@Override
		public LiterallyNamedAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new LiterallyNamedAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertAccessDelegateStrategy<PropertyAccessDelegate> DELEGATED_ACCESS_STRATEGY = new PropertAccessDelegateStrategy<PropertyAccessDelegate>() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Class<?> containerJavaType, String propertyName,
				Class<?>... parameterTypes) {
			return new PropertyAccessDelegate(containerJavaType, propertyName, false, parameterTypes);
		}

	};

	public static final LambdaPropertyAccessStrategy<Object, Object, Object, RuntimeException, Object, Object, FunctionalNoAccess> FUNCTIONAL_NO_ACCESS_STRATEGY = new LambdaPropertyAccessStrategy<>() {

		@Override
		public FunctionalNoAccess buildPropertyAccess(Object getter, Object setter) {
			return FunctionalNoAccess.INSTANCE;
		}

	};

	public static final <T, R, E extends RuntimeException> LambdaPropertyAccessStrategy<T, Object, R, E, HandledFunction<T, R, E>, HandledFunction<T, R, E>, FunctionalPropertyAccess<T, R, E>> createFunctionalAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public FunctionalPropertyAccess buildPropertyAccess(HandledFunction<T, R, E> getter,
					HandledFunction<T, R, E> setter) {
				return new FunctionalPropertyAccess<>(getter, setter, this);
			}

		};
	}

	public static final <F, S, R, E extends RuntimeException> LambdaPropertyAccessStrategy<F, S, R, E, HandledBiFunction<F, S, R, E>, HandledBiFunction<F, S, R, E>, BiFunctionalPropertyAccess<F, S, R, E>> createBiFunctionalAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public BiFunctionalPropertyAccess buildPropertyAccess(HandledBiFunction<F, S, R, E> getter,
					HandledBiFunction<F, S, R, E> setter) {
				return new BiFunctionalPropertyAccess<>(getter, setter, this);
			}

		};
	}

	public static final <T, E extends RuntimeException> LambdaPropertyAccessStrategy<T, Object, Object, E, HandledConsumer<T, E>, HandledConsumer<T, E>, ConsumingPropertyAccess<T, E>> createConsumingAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public ConsumingPropertyAccess buildPropertyAccess(HandledConsumer<T, E> getter,
					HandledConsumer<T, E> setter) {
				return new ConsumingPropertyAccess<>(getter, setter, this);
			}

		};
	}

	public static final <R, E extends RuntimeException> LambdaPropertyAccessStrategy<Object, Object, R, E, HandledSupplier<R, E>, HandledSupplier<R, E>, SupplyingPropertyAccess<R, E>> createSupplyingAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public SupplyingPropertyAccess buildPropertyAccess(HandledSupplier<R, E> getter,
					HandledSupplier<R, E> setter) {
				return new SupplyingPropertyAccess<>(getter, setter, this);
			}

		};
	}

	public static final <E extends RuntimeException> LambdaPropertyAccessStrategy<Object, Object, Object, E, Object, Object, MixedLambdaPropertyAccess<E>> createMixedAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public MixedLambdaPropertyAccess buildPropertyAccess(Object getter, Object setter) {
				return new MixedLambdaPropertyAccess<>(getter, setter, this);
			}

		};
	}

	public static final <F, S, R, E extends RuntimeException> HybridPropertyAccessStrategy<F, S, R, E> createHybridAccess() {
		return new HybridPropertyAccessStrategy<F, S, R, E>() {

			@Override
			public HybridAccess<F, S, R, E> buildPropertyAccess(Getter getter, Setter setter, Object getterLambda,
					Object setterLambda) {
				return new HybridAccess<>(getter, setter, getterLambda, setterLambda, this);
			}

		};
	}

	public interface LambdaPropertyAccessStrategy<FIRSTARG, SECONDARG, RETURN, EXCEPTION extends RuntimeException, GETTER, SETTER, ACCESS extends LambdaPropertyAccess<FIRSTARG, SECONDARG, RETURN, EXCEPTION, GETTER, SETTER>>
			extends PropertyAccessStrategy {

		@Override
		default PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return NO_ACCESS_STRATEGY.buildPropertyAccess(containerJavaType, propertyName);
		}

		ACCESS buildPropertyAccess(GETTER getter, SETTER setter);

	}

	public interface PropertyAccessStrategyImplementor<T extends PropertyAccessImplementor>
			extends PropertyAccessStrategy {

		@Override
		T buildPropertyAccess(Class containerJavaType, String propertyName);

	}

	public interface PropertAccessDelegateStrategy<T extends PropertyAccessImplementor>
			extends PropertyAccessStrategyImplementor<T> {

		@Override
		default T buildPropertyAccess(Class containerJavaType, String propertyName) {
			return buildPropertyAccess(containerJavaType, propertyName, Object.class);
		}

		T buildPropertyAccess(Class<?> containerJavaType, String propertyName, Class<?>... parameterTypes);

	}

	public interface PropertyAccessImplementor extends PropertyAccess {

		default boolean hasGetter() {
			return getGetter() != null && getGetter() != NoAccess.NO_OP_GETTER;
		}

		default boolean hasSetter() {
			return getSetter() != null && getSetter() != NoAccess.NO_OP_GETTER;
		}

		default String getLoggableString() {
			if (this instanceof LambdaPropertyAccess) {
				LambdaPropertyAccess access = (LambdaPropertyAccess) this;

				return String.format("%s(getter=[%s], setter=[%s])", this.getClass().getSimpleName(),
						access.getGetterFunction(), access.getSetterFunction());
			}

			return String.format("%s(getter=[%s], setter=[%s])", this.getClass().getSimpleName(), getGetter(),
					getSetter());
		}

	}

	public interface LambdaPropertyAccess<F, S, R, E extends RuntimeException, GETTER, SETTER>
			extends PropertyAccessImplementor {

		@Override
		default Getter getGetter() {
			return NoAccess.NO_OP_GETTER;
		}

		@Override
		default Setter getSetter() {
			return NoAccess.NO_OP_SETTER;
		}

		@Override
		default boolean hasGetter() {
			return hasGetterFunction();
		}

		@Override
		default boolean hasSetter() {
			return hasSetterFunction();
		}

		default boolean hasGetterFunction() {
			return getGetterFunction() != null && getGetterFunction() != FunctionalNoAccess.NO_OP;
		}

		default boolean hasSetterFunction() {
			return getSetterFunction() != null && getSetterFunction() != FunctionalNoAccess.NO_OP;
		}

		GETTER getGetterFunction();

		SETTER getSetterFunction();

		LambdaType getGetterType();

		LambdaType getSetterType();

		public enum LambdaType {

			FUNCTION, BIFUNCTION, CONSUMER, SUPPLIER, NO_ACCESS
		}

	}

	interface HybridPropertyAccessStrategy<F, S, R, E extends RuntimeException>
			extends LambdaPropertyAccessStrategy<F, S, R, E, Object, Object, HybridAccess<F, S, R, E>> {

		@Override
		default HybridAccess<F, S, R, E> buildPropertyAccess(Object getter, Object setter) {
			MixedLambdaPropertyAccess<RuntimeException> access = createMixedAccess().buildPropertyAccess(getter,
					setter);

			return new HybridAccess<>(null, null, access.getGetterFunction(), access.getSetterFunction(), this);
		}

		@Override
		default HybridAccess<F, S, R, E> buildPropertyAccess(Class containerJavaType, String propertyName) {
			PropertyAccess access = PropertyAccessStrategyFactory.DELEGATED_ACCESS_STRATEGY
					.buildPropertyAccess(containerJavaType, propertyName);

			return new HybridAccess<>(access.getGetter(), access.getSetter(), null, null, this);
		}

		HybridAccess<F, S, R, E> buildPropertyAccess(Getter getter, Setter setter, Object getterLambda,
				Object setterLambda);

	}

}
