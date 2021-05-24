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
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

/**
 * Getter and Setter of this strategy are no-op methods, they do absolutely
 * bloody nothing
 * 
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class NoAccess extends AbstractPropertyAccess {

	static final NoAccess INSTANCE = new NoAccess();

	private NoAccess() {
		super(NO_OP_GETTER, NO_OP_SETTER);
	}

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

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return PropertyAccessStrategyFactory.NO_ACCESS_STRATEGY;
	}

}
