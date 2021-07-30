/**
 * 
 */
package adn.service.resource.engine.tuple;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import org.hibernate.property.access.spi.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.engine.access.AbstractPropertyAccess;
import adn.engine.access.HybridAccess;
import adn.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess;
import adn.engine.access.PropertyAccessStrategyFactory.LambdaPropertyAccess.LambdaType;
import adn.engine.access.PropertyAccessStrategyFactory.PropertyAccessImplementor;
import adn.helpers.FunctionHelper.HandledBiFunction;
import adn.helpers.FunctionHelper.HandledFunction;
import adn.helpers.TypeHelper;
import adn.service.resource.engine.template.ResourceTemplate;
import adn.service.resource.engine.tuple.InstantiatorFactory.ParameterizedInstantiator;
import adn.service.resource.engine.tuple.InstantiatorFactory.PojoInstantiator;

/**
 * @author Ngoc Huy
 *
 */
public abstract class ResourceTuplizerContract implements ResourceTuplizer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected abstract ResourceTemplate getResourceTemplate();

	protected abstract PojoInstantiator<File> getInstantiator();

	@Override
	public void validate(Object[] values, String[] columnNames) {
		ResourceTemplate template = getResourceTemplate();
		int span = columnNames.length;

		Class<?>[] columnTypes = template.getColumnTypes();
		Class<?> registeredType, givenType;
		int columnIndex;

		for (int i = 0; i < span; i++) {
			columnIndex = template.getColumnIndex(columnNames[i]);

			if (values[i] == null && !template.isColumnNullable(columnIndex)) {
				throw new IllegalArgumentException(
						String.format("Found null value on column [%s] while registered non-nullable, template: [%s]",
								template.getColumnNames()[i], template.getTemplateName()));
			}

			registeredType = columnTypes[columnIndex];
			givenType = values[i].getClass();

			if (registeredType.equals(givenType)) {
				continue;
			}

			if (!TypeHelper.TYPE_CONVERTER.containsKey(registeredType)
					|| !TypeHelper.TYPE_CONVERTER.get(registeredType).containsKey(givenType)) {
				throw new IllegalArgumentException(
						String.format("Invalid value type, value type was [%s], registered type [%s] in template [%s]",
								givenType, registeredType, template.getTemplateName()));
			}
		}
	}

	/**
	 * <b>CONTRACT:</b>
	 * </p>
	 * A {@link File} instance must be, hence the <em>final</em> modifier, hydrated
	 * with a specific property hydrations order
	 * </p>
	 * The {@code values} parameters must therefore be ordered respectively to the
	 * registered columns in the {@link ResourceTemplate} contract. This method
	 * assumes that passed values respect the registered column types in the
	 * {@link ResourceTemplate}
	 */
	@Override
	public final void setPropertyValues(Object entity, Object[] values) {
		final File file = (File) entity;
		final ResourceTemplate template = getResourceTemplate();
		final int pathColumnIndex = template.getColumnIndex(template.getPathColumn());
		final int extensionColumnIndex = template.getColumnIndex(template.getExtensionColumn());
		// we don't have to invoke path and extension hydration since it can only been
		// done via constructor call
		int contentColumnIndex = -1; // indicates this resource has no content

		if (!template.getContentColumn().equals(ResourceTemplate.NO_CONTENT.toString())) {
			contentColumnIndex = template.getColumnIndex(template.getContentColumn());
		}

		final int span = template.getPropertySpan();

		for (int i = 0; i < span; i++) {
			if (i == pathColumnIndex || i == extensionColumnIndex || i == contentColumnIndex) {
				continue;
			}

			invokeSetAccess(template.getPropertyAccess(i), file, values[i]);
		}
		// lastly, we invoke content-set access to save the content
		if (contentColumnIndex != -1) {
			invokeSetAccess(template.getPropertyAccess(contentColumnIndex), file, values[contentColumnIndex]);
		}
	}

	@SuppressWarnings("unchecked")
	protected void invokeSetAccess(PropertyAccessImplementor access, File file, Object value) {
		if (access instanceof HybridAccess) {
			HybridAccess<?, ?, ?, ? extends RuntimeException> accessor = (HybridAccess<?, ?, ?, ? extends RuntimeException>) access;

			if (accessor.hasSetter()) {
				try {
					invokeSetter(accessor, file, value);
					return;
				} catch (RuntimeException re) {
					if (!accessor.hasSetterLambda()) {
						throw re;
					}

					logger.trace("Trying setter lambda");
				}
			}

			invokeSetterLambda(accessor, file, value, true);
			return;
		}

		if (access instanceof AbstractPropertyAccess) {
			invokeSetter((AbstractPropertyAccess) access, file, value);
			return;
		}

		invokeSetterLambda((LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?>) access, file, value, true);
	}

	@SuppressWarnings("unchecked")
	protected void invokeSetterLambda(LambdaPropertyAccess<?, ?, ?, ? extends RuntimeException, ?, ?> lambdaAccess,
			File instance, Object value, boolean shouldThrowLambdaTypeMismatchException) {
		if (!lambdaAccess.hasSetterLambda()) {
			return;
		}

		if (lambdaAccess.getSetterType().equals(LambdaType.FUNCTION)) {
			instance = ((HandledFunction<File, File, RuntimeException>) lambdaAccess.getSetterLambda()).apply(instance);
			return;
		}

		if (lambdaAccess.getSetterType().equals(LambdaType.BIFUNCTION)) {
			instance = ((HandledBiFunction<File, Object, File, RuntimeException>) lambdaAccess.getSetterLambda())
					.apply(instance, value);
			return;
		}

		if (shouldThrowLambdaTypeMismatchException) {
			throw new IllegalArgumentException(String.format(
					"Exception thrown while invoking setter lambda using value [%s]. Setter access must be of type [%s], type [%s] given",
					value, HandledFunction.class, lambdaAccess.getSetterLambda().getClass()));
		}
	}

	protected void invokeSetter(AbstractPropertyAccess accessor, File instance, Object value) throws RuntimeException {
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

	protected void checkTypeAndSet(File instance, Setter setter, Object value) throws SQLException {
		Class<?> paramType = setter.getMethod().getParameterTypes()[0];

		if (!paramType.equals(value.getClass())) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Casting [%s] -> [%s]", value.getClass(), paramType));
			}

			try {
				setter.set(instance, TypeHelper.TYPE_CONVERTER.get(paramType).get(value.getClass()).apply(value), null);
			} catch (RuntimeException rte) {
				rte.printStackTrace();
				throw new SQLException(rte);
			}

			return;
		}

		setter.set(instance, value, null);
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
	public File instantiate() {
		PojoInstantiator<File> instantiator = getInstantiator();

		if (instantiator instanceof ParameterizedInstantiator) {
			throw new IllegalArgumentException(String.format(
					"Unable to instaniate instance of type [%s] with no-param instantiator, built instantiator is [%s], template is [%s]",
					File.class, instantiator, getResourceTemplate().getTemplateName()));
		}

		return instantiator.instantiate();
	}

	@Override
	public File instantiate(Object[] values) {
		PojoInstantiator<File> instantiator = getInstantiator();

		if (instantiator instanceof ParameterizedInstantiator) {
			ParameterizedInstantiator<File> parameterizedInstantiator = (ParameterizedInstantiator<File>) instantiator;

			preInstantiate(values);

			File file = parameterizedInstantiator.instantiate(values);

			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Instantiated a new file [%s]", file.getPath()));
			}

			return file;
		}

		return instantiator.instantiate();
	}

	/**
	 * Before instantiating a resource, we need to prepend/assert the configured
	 * path of the storage and append the extension to the resource path. This
	 * method assumes that types of the <em>argumentValues</em> match the
	 * <em>argumentTypes</em>. Therefore callers must assert argument types before
	 * calling it
	 * </p>
	 * 
	 * This method can be contained with other processes if any, the whole point is
	 * to standardise property values, so that they respect the business
	 */
	private void preInstantiate(Object[] argumentValues) {
		Class<?>[] argumentTypes = getInstantiator().getParameterTypes();
		ResourceTemplate template = getResourceTemplate();
		String extension = (String) argumentValues[template.getColumnIndex(template.getExtensionColumn())];
		// TODO: asserts extension support??
		String templateDirectory = template.getDirectory();
		String path;
		int pathIndex = template.getColumnIndex(template.getPathColumn());
		// new File(String [,String])
		if (argumentTypes[pathIndex].equals(String.class)) {
			path = (String) argumentValues[pathIndex];
			// @formatter:off
			argumentValues[pathIndex] = new StringBuilder(templateDirectory)
					.append(argumentValues[pathIndex])
					.append(extension)
					.toString();
			// @formatter:on
			return;
		}
		// new File(URI)
		if (argumentTypes[pathIndex].equals(URI.class)) {
			path = ((URI) argumentValues[pathIndex]).getRawPath();

			if (path.startsWith(templateDirectory)) {
				return;
			}

			try {
				// @formatter:off
				argumentValues[pathIndex] = new URI(new StringBuilder(templateDirectory)
						.append(argumentValues[pathIndex])
						.append(extension)
						.toString());
				// @formatter:on
			} catch (URISyntaxException urise) {
				urise.printStackTrace();
				throw new IllegalArgumentException(urise);
			}

			return;
		}
		// new File(File, String)
		// since we aren't able to modify the path of a File instance, we can only do an
		// assertion on it's path
		path = ((File) argumentValues[pathIndex]).getAbsolutePath();

		Assert.isTrue(path.startsWith(templateDirectory),
				String.format("Invalid path [%s]. Template [%s] requires leading [%s]... in resource path", path,
						template.getTemplateName(), templateDirectory));
	}

}
