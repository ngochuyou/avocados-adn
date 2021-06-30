/**
 * 
 */
package adn.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledConsumer;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;

/**
 * Central factory for producing {@link PropertyAccessImplementor} instances
 * 
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("rawtypes")
public final class PropertyAccessStrategyFactory {
	// static only
	private PropertyAccessStrategyFactory() {}

	public static final PropertyAccessStrategyImplementor<NoAccess> NO_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<NoAccess>() {

		@Override
		public NoAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new NoAccess();
		};

		@Override
		public String toString() {
			return "NO_ACCESS_STRATEGY";
		}

	};

	public static final PropertyAccessStrategyImplementor<StandardAccess> STANDARD_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<StandardAccess>() {

		@Override
		public StandardAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new StandardAccess(containerJavaType, propertyName);
		};

		@Override
		public String toString() {
			return "STANDARD_ACCESS_STRATEGY";
		}

	};

	public static final PropertyAccessStrategyImplementor<DirectAccess> DIRECT_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<DirectAccess>() {

		@Override
		public DirectAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new DirectAccess(containerJavaType, propertyName);
		}

		@Override
		public String toString() {
			return "DIRECT_ACCESS_STRATEGY";
		}

	};

	public static final PropertyAccessStrategyImplementor<LiterallyNamedAccess> LITERALLY_NAMED_ACCESS_STRATEGY = new PropertyAccessStrategyImplementor<LiterallyNamedAccess>() {

		@Override
		public LiterallyNamedAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new LiterallyNamedAccess(containerJavaType, propertyName);
		}

		@Override
		public String toString() {
			return "LITERALLY_NAMED_ACCESS_STRATEGY";
		}

	};

	public static final PropertAccessDelegateStrategy DELEGATED_ACCESS_STRATEGY = new PropertAccessDelegateStrategy() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Class<?> containerJavaType, String propertyName,
				Class<?>... parameterTypes) {
			return new PropertyAccessDelegate(containerJavaType, propertyName, false, parameterTypes);
		}

		@Override
		public String toString() {
			return "DELEGATED_ACCESS_STRATEGY";
		}

	};

	public static final SpecificAccessStrategy SPECIFIC_ACCESS_STRATEGY = new SpecificAccessStrategy() {

		@Override
		public String toString() {
			return "SPECIFIC_ACCESS_STRATEGY";
		}

	};

	public static final LambdaPropertyAccessStrategy<Object, Object, Object, RuntimeException, Object, Object, FunctionalNoAccess> FUNCTIONAL_NO_ACCESS_STRATEGY = new LambdaPropertyAccessStrategy<>() {

		@Override
		public FunctionalNoAccess buildPropertyAccess(Object getter, Object setter) {
			return FunctionalNoAccess.INSTANCE;
		}

		@Override
		public String toString() {
			return "FUNCTIONAL_NO_ACCESS_STRATEGY";
		}

	};

	public static final <T, R, E extends RuntimeException> LambdaPropertyAccessStrategy<T, Object, R, E, HandledFunction<T, R, E>, HandledFunction<T, R, E>, FunctionalPropertyAccess<T, R, E>> createFunctionalAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public FunctionalPropertyAccess buildPropertyAccess(HandledFunction<T, R, E> getter,
					HandledFunction<T, R, E> setter) {
				return new FunctionalPropertyAccess<>(getter, setter, this);
			}

			@Override
			public String toString() {
				return "FunctionalPropertyAccess";
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

			@Override
			public String toString() {
				return "BiFunctionalPropertyAccess";
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

			@Override
			public String toString() {
				return "ConsumingPropertyAccess";
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

			@Override
			public String toString() {
				return "SupplyingPropertyAccess";
			}

		};
	}

	public static final <E extends RuntimeException> LambdaPropertyAccessStrategy<Object, Object, Object, E, Object, Object, MixedLambdaPropertyAccess<E>> createMixedAccess() {
		return new LambdaPropertyAccessStrategy<>() {

			@Override
			public MixedLambdaPropertyAccess buildPropertyAccess(Object getter, Object setter) {
				return new MixedLambdaPropertyAccess<>(getter, setter, this);
			}

			@Override
			public String toString() {
				return "MixedLambdaPropertyAccess";
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

			@Override
			public String toString() {
				return "HybridPropertyAccess";
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

	public interface SpecificAccessStrategy extends PropertyAccessStrategyImplementor<SpecificAccess> {

		@Override
		default SpecificAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new SpecificAccess(
					PropertyAccessDelegate.locateGetter(containerJavaType, propertyName, false)
							.orElseThrow(() -> new IllegalArgumentException(
									String.format("Unable to locate getter for property [%s] of type [%s]",
											propertyName, containerJavaType.getName()))),
					PropertyAccessDelegate.locateSetter(containerJavaType, propertyName, false)
							.orElseThrow(() -> new IllegalArgumentException(
									String.format("Unable to locate setter for property [%s] of type [%s]",
											propertyName, containerJavaType.getName()))));
		}

		default SpecificAccess buildPropertyAccess(Getter getter, Setter setter) {
			return new SpecificAccess(getter, setter);
		}
	}

	public interface PropertAccessDelegateStrategy extends PropertyAccessStrategyImplementor<PropertyAccessDelegate> {

		@Override
		default PropertyAccessDelegate buildPropertyAccess(Class containerJavaType, String propertyName) {
			return buildPropertyAccess(containerJavaType, propertyName, Object.class);
		}

		PropertyAccessDelegate buildPropertyAccess(Class<?> containerJavaType, String propertyName,
				Class<?>... parameterTypes);

	}

	public interface PropertyAccessImplementor extends PropertyAccess {

		default boolean hasGetter() {
			return getGetter() != null && getGetter() != NoAccess.NO_OP_GETTER;
		}

		default boolean hasSetter() {
			return getSetter() != null && getSetter() != NoAccess.NO_OP_SETTER;
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
			return hasGetterLambda();
		}

		@Override
		default boolean hasSetter() {
			return hasSetterLambda();
		}

		default boolean hasGetterLambda() {
			return getGetterLambda() != null && getGetterLambda() != FunctionalNoAccess.NO_OP;
		}

		default boolean hasSetterLambda() {
			return getSetterLambda() != null && getSetterLambda() != FunctionalNoAccess.NO_OP;
		}

		GETTER getGetterLambda();

		SETTER getSetterLambda();

		LambdaType getGetterType();

		LambdaType getSetterType();

		public enum LambdaType {

			FUNCTION, BIFUNCTION, CONSUMER, SUPPLIER, NO_ACCESS

		}

	}

	public interface HybridPropertyAccessStrategy<F, S, R, E extends RuntimeException>
			extends LambdaPropertyAccessStrategy<F, S, R, E, Object, Object, HybridAccess<F, S, R, E>> {

		@Override
		default HybridAccess<F, S, R, E> buildPropertyAccess(Object getter, Object setter) {
			MixedLambdaPropertyAccess<RuntimeException> access = createMixedAccess().buildPropertyAccess(getter,
					setter);

			return new HybridAccess<>(null, null, access.getGetterLambda(), access.getSetterLambda(), this);
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
