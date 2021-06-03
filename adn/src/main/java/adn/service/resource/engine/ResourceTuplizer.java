/**
 * 
 */
package adn.service.resource.engine;

import java.io.File;
import java.sql.SQLException;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.tuple.Tuplizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;
import adn.helpers.TypeHelper;
import adn.service.resource.engine.access.AbstractPropertyAccess;
import adn.service.resource.engine.access.HybridAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess.LambdaType;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.InstantiatorFactory.ParameterizedInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTuplizer implements Tuplizer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ResourceTemplate template;

	public ResourceTuplizer(ResourceTemplate template) {
		this.template = template;
	}

	@Override
	public Object[] getPropertyValues(Object entity) {
		return getPropertyValues(entity, null);
	}

	private void checkTypeAndSet(File instance, Setter setter, Object value) throws SQLException {
		Class<?> paramType = setter.getMethod().getParameterTypes()[0];

		if (!paramType.equals(value.getClass())) {
			if (!TypeHelper.TYPE_CONVERTER.containsKey(paramType)) {
				throw new SQLException(String.format("Type mismatch [%s><%s]", paramType, value.getClass()));
			}

			logger.trace(String.format("Casting [%s] -> [%s]", value.getClass(), paramType));
			setter.set(instance, TypeHelper.TYPE_CONVERTER.get(paramType).get(value.getClass()).apply(value), null);
			return;
		}

		setter.set(instance, value, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setPropertyValues(Object entity, Object[] values) {
		File file = (File) entity;

		int span = template.getColumnNames().length;
		PropertyAccessImplementor[] accessors = template.getPropertyAccessors();

		for (int i = 0; i < span; i++) {
			if (accessors[i] instanceof HybridAccess) {
				HybridAccess<?, ?, ?, ? extends RuntimeException> accessor = (HybridAccess<?, ?, ?, ? extends RuntimeException>) accessors[i];

				if (accessor.hasSetter()) {
					try {
						invokeSetter(accessor, file, values[i]);
						continue;
					} catch (RuntimeException re) {
						if (!accessor.hasSetterLambda()) {
							throw re;
						}

						logger.trace("Trying setter lambda");
					}
				}

				invokeSetterLambda(accessor, file, values[i], true);
				continue;
			}

			if (accessors[i] instanceof AbstractPropertyAccess) {
				invokeSetter((AbstractPropertyAccess) accessors[i], file, values[i]);
				continue;
			}

			invokeSetterLambda((LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?>) accessors[i], file,
					values[i], true);
		}
	}

	private Object invokeGetter(AbstractPropertyAccess accessor, File instance) throws RuntimeException {
		if (!accessor.hasGetter()) {
			return null;
		}

		try {
			return accessor.getGetter().get(instance);
		} catch (RuntimeException re) {
			logger.trace(String.format("Exception thrown while invoking getter [%s] with message [%s]",
					accessor.getGetter(), re.getMessage()));
			throw re;
		}
	}

	private void invokeSetter(AbstractPropertyAccess accessor, File instance, Object value) throws RuntimeException {
		if (!accessor.hasSetter()) {
			return;
		}

		try {
			checkTypeAndSet(instance, accessor.getSetter(), value);
		} catch (Exception any) {
			logger.trace(String.format("Exception thrown while invoking setter [%s] using value [%s] with message [%s]",
					accessor.getSetter(), value, any.getMessage()));
			throw new RuntimeException(any);
		}
	}

	@SuppressWarnings("unchecked")
	private Object invokeGetterLambda(LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?> lambdaAccess,
			File instance, boolean shouldThrowLambdaTypeMismatchException) {
		if (!lambdaAccess.hasGetterLambda()) {
			return null;
		}

		if (lambdaAccess.getGetterType().equals(LambdaType.FUNCTION)) {
			return ((HandledFunction<File, ?, RuntimeException>) lambdaAccess.getGetterLambda()).apply(instance);
		}

		if (lambdaAccess.getGetterType().equals(LambdaType.SUPPLIER)) {
			return ((HandledSupplier<?, RuntimeException>) lambdaAccess.getGetterLambda()).get();
		}

		if (shouldThrowLambdaTypeMismatchException) {
			throw new IllegalArgumentException(String.format("Getter access must be of type [%s], type[%s] given",
					HandledSupplier.class, lambdaAccess.getGetterLambda().getClass()));
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private void invokeSetterLambda(LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?> lambdaAccess,
			File instance, Object value, boolean shouldThrowLambdaTypeMismatchException) {
		if (!lambdaAccess.hasSetterLambda()) {
			return;
		}

		if (lambdaAccess.getSetterType().equals(LambdaType.FUNCTION)) {
			((HandledFunction<File, ?, RuntimeException>) lambdaAccess.getSetterLambda()).apply(instance);
			return;
		}

		if (shouldThrowLambdaTypeMismatchException) {
			throw new IllegalArgumentException(String.format(
					"Exception thrown while invoking setter lambda using value [%s]. Setter access must be of type [%s], type [%s] given",
					value, HandledFunction.class, lambdaAccess.getSetterLambda().getClass()));
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getPropertyValue(Object entity, int i) {
		File instance = (File) entity;
		PropertyAccessImplementor pa = template.getPropertyAccessors()[i];

		if (pa instanceof HybridAccess) {
			HybridAccess<?, ?, ?, ? extends RuntimeException> access = (HybridAccess<?, ?, ?, ? extends RuntimeException>) pa;

			if (access.hasGetter()) {
				return invokeGetter(access, instance);
			}

			return invokeGetterLambda(access, instance, false);
		}

		if (pa instanceof AbstractPropertyAccess) {
			return invokeGetter((AbstractPropertyAccess) pa, instance);
		}

		return invokeGetterLambda((LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?>) pa, instance,
				false);
	}

	@Override
	public File instantiate() {
		if (template.getInstantiator() instanceof ParameterizedInstantiator) {
			throw new IllegalArgumentException(String.format(
					"Unable to instaniate instance of type [%s] with no-param instantiator, built instantiator is [%s]",
					File.class, template.getInstantiator()));
		}

		return template.getInstantiator().instantiate();
	}

	@Override
	public boolean isInstance(Object object) {
		return getMappedClass().isAssignableFrom(object.getClass());
	}

	@Override
	public final Class<?> getMappedClass() {
		return File.class;
	}

	@Override
	public Getter getGetter(int i) {
		PropertyAccessImplementor pa = template.getPropertyAccessors()[i];

		return pa instanceof AbstractPropertyAccess ? pa.getGetter() : null;
	}

	@SuppressWarnings("unchecked")
	public Object[] getPropertyValues(Object entity, String[] columnOrder) {
		File instance = (File) entity;
		int span = columnOrder == null ? template.getColumnNames().length : columnOrder.length;
		Object[] values = new Object[span];
		PropertyAccessImplementor[] accessors = template.getPropertyAccessors().clone();
		PropertyAccessImplementor currentAccessor;

		for (int i = 0; i < span; i++) {
			currentAccessor = columnOrder != null ? template.getPropertyAccessor(columnOrder[i]) : accessors[i];

			if (currentAccessor instanceof HybridAccess) {
				HybridAccess<?, ?, ?, RuntimeException> accessor = (HybridAccess<?, ?, ?, RuntimeException>) currentAccessor;

				if (accessor.hasGetter()) {
					try {
						values[i] = invokeGetter(accessor, instance);
						continue;
					} catch (RuntimeException re) {
						if (!accessor.hasGetterLambda()) {
							throw re;
						}

						logger.trace("Trying getter lambda");
					}
				}

				values[i] = invokeGetterLambda(accessor, instance, true);
				continue;
			}

			if (currentAccessor instanceof AbstractPropertyAccess) {
				values[i] = invokeGetter((AbstractPropertyAccess) currentAccessor, instance);
				continue;
			}

			values[i] = invokeGetterLambda(
					(LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?>) currentAccessor, instance, true);
		}

		return values;
	}

}
