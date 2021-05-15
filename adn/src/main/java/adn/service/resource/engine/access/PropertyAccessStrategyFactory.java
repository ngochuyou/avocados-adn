/**
 * 
 */
package adn.service.resource.engine.access;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

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
			return new LiterallyNamedAccess(containerJavaType, propertyName);
		}

	};

	public static final PropertyAccessStrategy DELEGATE_ACCESS_STRATEGY = new PropertyAccessStrategy() {

		@Override
		public PropertyAccess buildPropertyAccess(Class containerJavaType, String propertyName) {
			PropertyAccess access;

			try {
				access = new StandardAccess(containerJavaType, propertyName);

				return access;
			} catch (IllegalArgumentException iae) {}

			return new PropertyAccessDelegateImpl(locateGetter(containerJavaType, propertyName),
					locateSetter(containerJavaType, propertyName));
		}

		private Getter locateGetter(Class containerJavaType, String propertyName) {
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

		private Setter locateSetter(Class containerJavaType, String propertyName) {
			Setter setter = StandardAccess.locateSetter(containerJavaType, propertyName).orElse(null);

			if (setter != null) {
				return setter;
			}

			setter = LiterallyNamedAccess.locateSetter(containerJavaType, propertyName).orElse(null);

			if (setter != null) {
				return setter;
			}

			return DirectAccess.locateSetter(containerJavaType, propertyName).orElse(NoAccess.NO_OP_SETTER);
		}

	};

	interface PropertyAccessDelegate extends PropertyAccess {

		default boolean hasGetter() {
			return getGetter() != null && getGetter() != NoAccess.NO_OP_GETTER;
		}

		default boolean hasSetter() {
			return getSetter() != null && getSetter() != NoAccess.NO_OP_SETTER;
		}

	}

	static class PropertyAccessDelegateImpl implements PropertyAccessDelegate {

		private final Getter getter;
		private final Setter setter;

		PropertyAccessDelegateImpl(Getter getter, Setter setter) {
			super();
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		public PropertyAccessStrategy getPropertyAccessStrategy() {
			return DELEGATE_ACCESS_STRATEGY;
		}

		@Override
		public Getter getGetter() {
			return getter;
		}

		@Override
		public Setter getSetter() {
			return setter;
		}

	}

}
