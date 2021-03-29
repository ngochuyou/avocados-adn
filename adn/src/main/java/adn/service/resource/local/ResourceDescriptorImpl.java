/**
 * 
 */
package adn.service.resource.local;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.Session;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.GetterMethodImpl;
import org.hibernate.property.access.spi.Setter;
import org.hibernate.property.access.spi.SetterMethodImpl;
import org.hibernate.tuple.GenerationTiming;
import org.springframework.util.Assert;

import adn.service.resource.models.NamedResource;
import adn.utilities.StringHelper;

/**
 * @author Ngoc Huy
 *
 */
public class ResourceDescriptorImpl<T> implements ResourceDescriptor<T> {

	private final Class<T> type;

	private final Getter idGetter;

	private final Setter idSetter;

	private final boolean isIdAutoGenerated;

	private final AnnotationBasedResourceValueGeneration idGeneration;

	private final ResourceManagerFactory factory;

	private static final String NULL_STRING = "null";

	/**
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * 
	 */
	public ResourceDescriptorImpl(Class<T> type, ResourceManagerFactory resourceManagerFactory)
			throws IllegalArgumentException, SecurityException, NoSuchMethodException {
		// TODO Auto-generated constructor stub
		Assert.notNull(type, "Resource type cannot be null");
		this.type = type;
		// Instantiate identifier getter/setter via @Id
		Field idField = null;

		for (Field f : type.getDeclaredFields()) {
			if (f.getDeclaredAnnotation(Id.class) != null) {
				idField = f;
				break;
			}
		}

		if (idField == null) {
			throw new IllegalArgumentException("Could not find resource identifier: " + type.getName());
		}

		Assert.isTrue(Serializable.class.isAssignableFrom(idField.getType()),
				"Resource identifier must be Serializable");

		isIdAutoGenerated = idField.getDeclaredAnnotation(GeneratedValue.class) != null;

		if (isIdAutoGenerated) {
			AnnotationBasedResourceValueGeneration generation = ResourceIdentifierValueGeneration.INSTANCE;

			generation.initialize(null, Date.class);
			idGeneration = generation;
		} else {
			idGeneration = null;
		}

		Method method = type.getDeclaredMethod(
				StringHelper.toCamel("get " + idField.getName(), StringHelper.MULTIPLE_MATCHES_WHITESPACE_CHARS));

		idGetter = new GetterMethodImpl(type, idField.getName(), method);
		method = type.getDeclaredMethod(
				StringHelper.toCamel("set " + idField.getName(), StringHelper.MULTIPLE_MATCHES_WHITESPACE_CHARS),
				idField.getType());
		idSetter = new SetterMethodImpl(type, idField.getName(), method);
		factory = resourceManagerFactory;
	}

	@Override
	public void setIdentifier(T instance, Serializable identifier) {
		// TODO Auto-generated method stub
		idSetter.set(instance, identifier, null);
	}

	@Override
	public Serializable getIdentifier(T owner) {
		// TODO Auto-generated method stub
		Object identifier = idGetter.get(owner);

		if (identifier instanceof String && String.valueOf(identifier).equals(NULL_STRING)) {
			return null;
		}

		return (Serializable) identifier;
	}

	@Override
	public Class<T> getType() {
		// TODO Auto-generated method stub
		return type;
	}

	@Override
	public Getter getIdentifierGetter() {
		return idGetter;
	}

	@Override
	public Setter getIdentifierSetter() {
		return idSetter;
	}

	@Override
	public boolean isInstance(Class<? extends T> type) {
		// TODO Auto-generated method stub
		return this.type.isAssignableFrom(type);
	}

	@Override
	public boolean isIdentifierAutoGenerated() {
		// TODO Auto-generated method stub
		return isIdAutoGenerated;
	}

	@Override
	public AnnotationBasedResourceValueGeneration getIdentifierValueGeneration() {
		// TODO Auto-generated method stub
		return idGeneration;
	}

	private static class ResourceIdentifierValueGenerator implements ResourcePropertyValueGenerator<Serializable> {

		public static final ResourceIdentifierValueGenerator INSTANCE = new ResourceIdentifierValueGenerator();

		public static final String IDENTIFIER_PARTS_SEPERATOR = "_";

		@Override
		@Deprecated
		public Serializable generateValue(Session session, Object owner) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException(ResourceManagerFactory.HIBERNATE_UNSUPPORTED);
		}

		@Override
		public Serializable generateValue(ResourceManagerFactory factory, Object object) {
			// TODO Auto-generated method stub
			if (object instanceof NamedResource) {
				// @formatter:off
				NamedResource instance = (NamedResource) object;
				
				return new StringBuilder(instance.getDirectoryPath())
						.append(new Date().getTime())
						.append(IDENTIFIER_PARTS_SEPERATOR)
						.append(StringHelper.hash(instance.getName()))
						.append(instance.getExtension())
						.toString();
				// @formatter:on
			}

			return String.valueOf(new Date().getTime());
		}

	}

	private static class ResourceIdentifierValueGeneration implements AnnotationBasedResourceValueGeneration {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public static final ResourceIdentifierValueGeneration INSTANCE = new ResourceIdentifierValueGeneration();

		private final ResourcePropertyValueGenerator<Serializable> generator = ResourceIdentifierValueGenerator.INSTANCE;

		@Override
		public GenerationTiming getGenerationTiming() {
			// TODO Auto-generated method stub
			return GenerationTiming.ALWAYS;
		}

		@Override
		public ResourcePropertyValueGenerator<Serializable> getValueGenerator() {
			// TODO Auto-generated method stub
			return generator;
		}

		@Override
		public boolean referenceColumnInSql() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException(ResourceManagerFactory.HIBERNATE_UNSUPPORTED);
		}

		@Override
		public String getDatabaseGeneratedReferencedColumnValue() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException(ResourceManagerFactory.HIBERNATE_UNSUPPORTED);
		}

		@Override
		public void initialize(ResourceIdentifier annotation, Class<?> propertyType) {
			// TODO Auto-generated method stub
		}

	}

	@Override
	public ResourceManagerFactory getResourceManagerFactory() {
		// TODO Auto-generated method stub
		return factory;
	}

}
