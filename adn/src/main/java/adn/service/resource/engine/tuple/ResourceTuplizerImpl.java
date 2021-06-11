/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.io.File;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.Tuplizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.FunctionHelper.HandledSupplier;
import adn.service.resource.engine.access.AbstractPropertyAccess;
import adn.service.resource.engine.access.HybridAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess.LambdaType;
import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceTuplizerImpl extends ResourceTuplizerContract implements Tuplizer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final ResourceTemplate template;

	private final PojoInstantiator<File> instantiator;

	public ResourceTuplizerImpl(ResourceTemplate template, PojoInstantiator<File> instantiator) {
		this.template = template;
		this.instantiator = instantiator;
	}

	@Override
	protected ResourceTemplate getResourceTemplate() {
		return template;
	}

	@Override
	protected PojoInstantiator<File> getInstantiator() {
		return instantiator;
	}

	@Override
	public Object[] getPropertyValues(Object entity) {
		return getPropertyValues(entity, null);
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
	public Getter getGetter(int i) {
		PropertyAccessImplementor pa = template.getPropertyAccessors()[i];

		return pa instanceof AbstractPropertyAccess ? pa.getGetter() : null;
	}

	@SuppressWarnings("unchecked")
	@Deprecated
	public Object[] getPropertyValues(Object entity, String[] columnOrder) {
		File instance = (File) entity;
		int span = columnOrder == null ? template.getColumnNames().length : columnOrder.length;
		Object[] values = new Object[span];
		PropertyAccessImplementor[] accessors = template.getPropertyAccessors().clone();
		PropertyAccessImplementor currentAccessor;

		for (int i = 0; i < span; i++) {
			currentAccessor = columnOrder != null ? template.getPropertyAccess(columnOrder[i]) : accessors[i];

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
