/**
 * 
 */
package adn.service.resource.engine.access;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.hibernate.internal.util.MarkerObject;
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

	public static final Set<Class<?>> GENERAL_DATE_ALTERNATIVE_TYPE_SET = Set.of(Long.class, long.class,
			Timestamp.class, Calendar.class, Time.class);
	// @formatter:off
	public static final Map<Class<?>, Set<Class<?>>> TYPE_ALTERNATIVES_MAP = Map.of(
			Date.class, GENERAL_DATE_ALTERNATIVE_TYPE_SET,
			java.sql.Date.class, GENERAL_DATE_ALTERNATIVE_TYPE_SET
		); 
	// @formatter:on
	public static final PropertyAccessStrategy NO_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		private final PropertyAccess NO_ACCESS = new NoAccess();

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			return NO_ACCESS;
		}

	};

	public static final PropertyAccessStrategy DIRECT_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new DirectAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategy STANDARD_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new StandardAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategy LITERALLY_NAMED_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Class containerJavaType, String propertyName) {
			return new LiterallyNamedAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategy DELEGATE_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Class containerJavaType, String propertyName) {
			try {
				return new StandardAccess(containerJavaType, propertyName);
			} catch (IllegalArgumentException iae) {
				return new PropertyAccessDelegateImpl(locateGetter(containerJavaType, propertyName, true),
						locateSetter(containerJavaType, propertyName, true));
			}
		}

	};

	public static final SpecificPropertyAccessStrategy SPECIFIC_ACCESS_STRATEGY = new SpecificPropertyAccessStrategy() {

		@Override
		public PropertyAccessDelegate buildPropertyAccess(Getter getter, Setter setter) {
			return new PropertyAccessDelegateImpl(getter, setter).setPropertyAccessStrategy(this);
		}

	};

	public static final FunctionPropertyAccessStrategy FUNCTIONAL_ACCESS_STRATEGY = new FunctionPropertyAccessStrategy() {

		@Override
		public <T, R, E extends Throwable> PropertyAccessDelegate buildPropertyAccess(HandledFunction<T, R, E> getter,
				HandledFunction<T, R, E> setter) {
			return new FunctionalPropertyAccessImpl<>(getter, setter);
		}

	};

	public static final HybridPropertyAccessStrategy HYBRID_ACCESS_STRATEGY = new HybridPropertyAccessStrategy() {

		@Override
		public <T, R, E extends Throwable> PropertyAccessDelegate buildPropertyAccess(Getter getter, Setter setter,
				HandledFunction<T, R, E> getterFnc, HandledFunction<T, R, E> setterFnc) {
			return new FunctionalPropertyAccessImpl<>(getterFnc, setterFnc).setGetter(getter).setSetter(setter)
					.setPropertyAccessStrategy(this);
		}
	};

	interface DelegateDefaultAccessStrategy extends PropertyAccessStrategy {

		@Override
		default PropertyAccessDelegate buildPropertyAccess(Class containerJavaType, String propertyName) {
			return (PropertyAccessDelegate) DELEGATE_ACCESS_STRATEGY.buildPropertyAccess(containerJavaType, propertyName);
		}

	}

	public interface SpecificPropertyAccessStrategy extends PropertyAccessStrategy, DelegateDefaultAccessStrategy {

		PropertyAccess buildPropertyAccess(Getter getter, Setter setter);

	}

	public interface FunctionPropertyAccessStrategy extends PropertyAccessStrategy, DelegateDefaultAccessStrategy {

		<T, R, E extends Throwable> PropertyAccessDelegate buildPropertyAccess(HandledFunction<T, R, E> getter,
				HandledFunction<T, R, E> setter);

	}

	public interface HybridPropertyAccessStrategy extends PropertyAccessStrategy, DelegateDefaultAccessStrategy {

		<T, R, E extends Throwable> PropertyAccessDelegate buildPropertyAccess(Getter getter, Setter setter,
				HandledFunction<T, R, E> getterFnc, HandledFunction<T, R, E> setterFnc);

	}

	interface PropertyAccessBuilder extends PropertyAccessDelegate {

		PropertyAccessBuilder setGetter(Getter getter);

		PropertyAccessBuilder setSetter(Setter setter);

		PropertyAccessBuilder setFieldRequired(boolean isFieldRequired);

		PropertyAccessBuilder setPropertyAccessStrategy(PropertyAccessStrategy strategy);

	}

	public interface PropertyAccessDelegate extends PropertyAccess {

		default boolean hasGetter() {
			return getGetter() != null && getGetter() != NoAccess.NO_OP_GETTER;
		}

		default boolean hasSetter() {
			return getSetter() != null && getSetter() != NoAccess.NO_OP_SETTER;
		}

	}

	public interface FunctionalPropertyAccess<T, R, E extends Throwable> extends PropertyAccessDelegate {

		HandledFunction<T, R, E> getGetterFunction();

		HandledFunction<T, R, E> getSetterFunction();

		FunctionalPropertyAccess<T, R, E> setGetterFunction(HandledFunction<T, R, E> getter);

		FunctionalPropertyAccess<T, R, E> setSetterFunction(HandledFunction<T, R, E> setter);

		default boolean hasGetterFunction() {
			return getGetterFunction() != null && getGetterFunction() != NO_OP_FNC;
		}

		default boolean hasSetterFunction() {
			return getSetterFunction() != null && getSetterFunction() != NO_OP_FNC;
		}

		public static final HandledFunction NO_OP_FNC = new HandledFunction<>() {
			@Override
			public Object apply(Object arg) throws Throwable {
				return new MarkerObject("NO_OP_FNC");
			}
		};

	}

	public static Getter locateGetter(Class containerJavaType, String propertyName, boolean isFieldRequired) {
		// @formatter:off
		return StandardAccess.locateGetter(containerJavaType, propertyName, isFieldRequired)
				.orElse(LiterallyNamedAccess.locateGetter(containerJavaType, propertyName)
						.orElse(DirectAccess.locateGetter(containerJavaType, propertyName)
								.orElse(NoAccess.NO_OP_GETTER)));
		// @formatter:on
	}

	public static Setter locateSetter(Class containerJavaType, String propertyName, boolean isFieldRequired,
			Class<?>... paramTypes) {
		// @formatter:off
		return StandardAccess.locateSetter(containerJavaType, propertyName, isFieldRequired, paramTypes)
				.orElse(LiterallyNamedAccess.locateSetter(containerJavaType, propertyName, paramTypes)
						.orElse(DirectAccess.locateSetter(containerJavaType, propertyName)
								.orElse(NoAccess.NO_OP_SETTER)));
		// @formatter:on
	}

}
