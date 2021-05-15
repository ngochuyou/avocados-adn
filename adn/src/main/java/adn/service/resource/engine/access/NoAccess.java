/**
 * 
 */
package adn.service.resource.engine.access;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class NoAccess implements PropertyAccess {

	static final Getter NO_OP_GETTER = new Getter() {

		@Override
		public Class<?> getReturnType() {
			return Void.class;
		}

		@Override
		public String getMethodName() {
			return "get";
		}

		@Override
		public Method getMethod() {
			try {
				return this.getClass().getDeclaredMethod(getMethodName());
			} catch (NoSuchMethodException | SecurityException e) {
				return null;
			}
		}

		@Override
		public Member getMember() {
			return getMethod();
		}

		@Override
		public Object getForInsert(Object owner, @SuppressWarnings("rawtypes") Map mergeMap,
				SharedSessionContractImplementor session) {
			return null;
		}

		@Override
		public Object get(Object owner) {
			return null;
		}
	};

	static final Setter NO_OP_SETTER = new Setter() {

		@Override
		public void set(Object target, Object value, SessionFactoryImplementor factory) {}

		@Override
		public String getMethodName() {
			return "set";
		}

		@Override
		public Method getMethod() {
			try {
				return this.getClass().getDeclaredMethod(getMethodName(), Object.class, Object.class,
						SharedSessionContractImplementor.class);
			} catch (NoSuchMethodException | SecurityException e) {
				return null;
			}
		}
	};

	NoAccess() {}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.NO_ACCESS_STRATEGY;
	}

	@Override
	public Getter getGetter() {
		return NO_OP_GETTER;
	}

	@Override
	public Setter getSetter() {
		return NO_OP_SETTER;
	}

}
