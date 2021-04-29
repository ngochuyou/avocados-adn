/**
 * 
 */
package adn.service.resource.storage;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ngoc Huy
 *
 */
public interface SupportedResourceContext extends AutoCloseable {

	public static final SupportedResourceContextImpl INSTANCE = new SupportedResourceContextImpl();

	boolean isSupported(Class<?> type);

	void contribute(Class<?> newType) throws IllegalAccessException;

	class SupportedResourceContextImpl implements SupportedResourceContext {

		private final Set<Class<?>> supportedTypes = new HashSet<>();

		private volatile boolean isClosed;

		private SupportedResourceContextImpl() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean isSupported(Class<?> type) {
			// TODO Auto-generated method stub
			return supportedTypes.contains(type);
		}

		private void checkAccess() throws IllegalAccessException {
			if (isClosed) {
				throw new IllegalAccessException(SupportedResourceContext.class.getName() + " was already closed");
			}
		}

		@Override
		public void contribute(Class<?> newType) throws IllegalAccessException {
			// TODO Auto-generated method stub
			checkAccess();
			supportedTypes.add(newType);
		}

		@Override
		public synchronized void close() throws IllegalAccessException {
			// TODO Auto-generated method stub
			checkAccess();
			isClosed = true;
		}

	}

}
