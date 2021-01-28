/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.PersistenceException;
import javax.persistence.Version;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.FileResource;
import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
public class PojoResourceTuplizer implements ResourceTuplizer {

	private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final transient int NO_VERSION_INDEX = -1;

	protected final transient MetamodelImpl metamodel;

	protected final Getter idGetter;
	protected final Setter idSetter;

	protected final Attribute<?, ?> identifierProperty;
	protected final int propertySpan;
	protected final Attribute<?, ?>[] properties;
	protected final Map<String, Integer> propertyIndices;
	protected final boolean isVersioned;
	protected final boolean isInherited;
	protected final int versionPropertyIndex;
	protected final Getter[] getters;
	protected final Setter[] setters;

	@SuppressWarnings({ "unchecked" })
	public <T, X> PojoResourceTuplizer(MetamodelImpl metamodel, Class<T> resourceClass, Class<X> identifierClass)
			throws PersistenceException, NoSuchMethodException, SecurityException {
		super();
		Assert.notNull(metamodel, "Metamodel cannot be null");
		Assert.notNull(resourceClass, "Resource class cannot be null");
		Assert.notNull(identifierClass, "Identifier class cannot be null");
		this.metamodel = metamodel;

		ManagedType<T> resourceType = metamodel.managedType(resourceClass);

		if (!(resourceType instanceof EntityType)) {
			throw new PersistenceException("Unsupported resource type: " + resourceClass.getName());
		}

		EntityType<T> entityType = (EntityType<T>) resourceType;
		Type<X> identifierType = (Type<X>) entityType.getIdType();

		if (identifierType == null) {
			throw new PersistenceException("Unnable to find identifier attribute of type: " + resourceClass.getName());
		}

		if (identifierType.getJavaType() != identifierClass) {
			throw new PersistenceException(
					String.format("Unsupported indentifier type: %s of type: %s. Expect identifier of type: %s",
							identifierClass.getName(), entityType.getJavaType().getName(),
							identifierType.getJavaType().getName()));
		}

		String identifierName = (identifierProperty = entityType.getDeclaredId(identifierType.getJavaType())).getName();
		String methodName = "";
		Method getterMethod;

		try {
			getterMethod = entityType.getJavaType()
					.getDeclaredMethod(methodName = Strings.toCamel("get " + identifierName, " "));
			idGetter = new GetterMethod(getterMethod);
		} catch (NoSuchMethodException nsme) {
			throw new NoSuchMethodException(
					String.format("Unnable to locate getter for identifier of type: %s, tried: %s",
							resourceClass.getName(), methodName));
		}

		Method setterMethod;

		try {
			setterMethod = entityType.getJavaType().getDeclaredMethod(
					methodName = Strings.toCamel("set " + identifierName, " "), identifierType.getJavaType());
			idSetter = new SetterMethod(setterMethod);
		} catch (NoSuchMethodException nsme) {
			throw new NoSuchMethodException(String.format(
					"Unnable to locate setter for identifier of type: %s, tried: %s with identifier type: %s",
					resourceClass.getName(), methodName, identifierType.getJavaType().getName()));
		}

		propertySpan = entityType.getAttributes().size();
		properties = new Attribute<?, ?>[propertySpan];

		Set<String> managedAttributeNames = entityType.getAttributes().stream().map(ele -> ele.getName())
				.collect(Collectors.toSet());
		int currentIndex = 0;
		Field[] declaredFields = entityType.getJavaType().getDeclaredFields();

		getters = new Getter[propertySpan];
		setters = new Setter[propertySpan];

		for (Field f : declaredFields) {
			if (managedAttributeNames.contains(f.getName())) {
				properties[currentIndex] = entityType.getAttributes().stream()
						.filter(attr -> attr.getName().equals(f.getName())).findFirst().orElseThrow();

				try {
					getterMethod = entityType.getJavaType()
							.getDeclaredMethod(Strings.toCamel(methodName = "get " + f.getName(), " "));
					getters[currentIndex] = new GetterMethod(getterMethod);
				} catch (NoSuchMethodException nsme) {
					throw new NoSuchMethodException(
							String.format("Unnable to locate getter for attribute of type: %s, tried: %s",
									f.getType().getName(), methodName));
				}

				try {
					setterMethod = entityType.getJavaType()
							.getDeclaredMethod(Strings.toCamel(methodName = "set " + f.getName(), " "), f.getType());
					setters[currentIndex] = new SetterMethod(setterMethod);
				} catch (NoSuchMethodException nsme) {
					throw new NoSuchMethodException(
							String.format("Unnable to locate getter for attribute of type: %s, tried: %s",
									f.getType().getName(), methodName));
				}

				currentIndex++;
			}
		}

		propertyIndices = new HashMap<>(propertySpan);

		for (int i = 0; i < propertySpan; i++) {
			propertyIndices.put(properties[i].getName(), i);
		}

		isVersioned = entityType.hasVersionAttribute();

		if (isVersioned) {
			VersionPropertyResolver versionPropertyResolver = new AnnotatedEntityVersionPropertyResolver();
			Attribute<?, ?> versionAttribute = versionPropertyResolver.resolve(entityType);

			versionPropertyIndex = versionAttribute != null ? propertyIndices.get(versionAttribute.getName())
					: NO_VERSION_INDEX;
		} else {
			versionPropertyIndex = NO_VERSION_INDEX;
		}

		isInherited = entityType.getSupertype() != null;
		// @formatter:off
		logger.debug(String.format("\nCreated %s for %s with following meta:\n"
				+ "\t-identifierProperty: %s\n"
				+ "\t-idGetter: %s\n"
				+ "\t-idSetter: %s\n"
				+ "\t-propertySpan: %d\n"
				+ "\t-properties: %s\n"
				+ "\t-propertyIndices: %s\n"
				+ "\t-getters ([index|memberName|returnType]):\n%s\n"
				+ "\t-setters ([index|memberName]):\n%s\n"
				+ "\t-isVersioned: %b\n"
				+ "\t-isInherited: %b\n",
						this.getClass().getName(), entityType.getJavaType().getName(),
						identifierProperty.getName(),
						idGetter.getClass().getName(),
						idSetter.getClass().getName(),
						propertySpan,
						Stream.of(properties).map(attr -> attr.getName())
							.collect(Collectors.joining(", ")),
						propertyIndices.entrySet()
							.stream().map(entry -> "[" + entry.getKey() + '|' + entry.getValue() + "]")
							.collect(Collectors.joining(", ")),
						String.format(
								"\t\t+span: %d\n"
								+ "\t\t+elements: %s",
								getters.length,
								Stream.of(getters)
									.map(getter -> "[" + propertyIndices.get(Strings.removeFirstCamelWord(getter.getMethodName())) + '|' +
											getter.getMethodName() + '|' +
											getter.getReturnType().getSimpleName() + "]")
									.collect(Collectors.joining(", "))),
						String.format(
								"\t\t+span: %d\n"
								+ "\t\t+elements: %s",
								setters.length,
								Stream.of(setters)
									.map(setter -> "[" + propertyIndices.get(Strings.removeFirstCamelWord(setter.getMethodName())) + '|' +
											setter.getMethodName() + "]")
									.collect(Collectors.joining(", "))), 
						isVersioned,
						isInherited));
		// @formatter:on
	}

	@Override
	public Serializable getIdentifier(Object resource)
			throws ClassCastException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		try {
			return (Serializable) idGetter.get(resource);
		} catch (ClassCastException cce) {
			throw new ClassCastException("Resource identifier must be of type Serializable");
		}
	}

	@Override
	public Object[] getPropertyValues(Object resource) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPropertyValues(Object resource, Object[] values)
			throws IllegalAccessException, IllegalArgumentException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getPropertyValue(Object resource, int i) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getPropertyValue(Object resource, String propertyName) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPropertyValue(Object resource, int i, Object value) throws NoSuchMethodException, SecurityException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object resource, String propertyName, Object value) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object instantiate() throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInstance(Object resource) {
		// TODO Auto-generated method stub
		return resource instanceof FileResource;
	}

	@Override
	public Class<?> getMappedClass() {
		// TODO Auto-generated method stub
		return FileResource.class;
	}

	@Override
	public void setIdentifier(Object resource, Serializable value)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		// TODO Auto-generated method stub
		idSetter.set(value, resource);
	}

	@Override
	public int getPropertySpan() {
		// TODO Auto-generated method stub
		return propertySpan;
	}

	protected interface VersionPropertyResolver {

		<X, Y> Attribute<X, Y> resolve(Type<X> type) throws NoSuchElementException, IllegalArgumentException;

	}

	protected class AnnotatedEntityVersionPropertyResolver implements VersionPropertyResolver {

		@SuppressWarnings("unchecked")
		@Override
		public <X, Y> Attribute<X, Y> resolve(Type<X> type) {
			// TODO Auto-generated method stub
			if (!(type instanceof EntityType)) {
				throw new IllegalArgumentException(
						String.format("Unsupported type: %s passed in VersionPropertyResolver: %s",
								type.getClass().getName(), this.getClass().getName()));
			}

			EntityType<X> entityType = (EntityType<X>) type;

			Field[] declaredFields = type.getJavaType().getDeclaredFields();

			for (Field f : declaredFields) {
				if (f.getDeclaredAnnotation(Version.class) != null) {

					return (Attribute<X, Y>) entityType.getDeclaredAttributes().stream()
							.filter(attr -> attr.getName() == f.getName()).findFirst().orElseThrow();
				}
			}

			throw new NoSuchElementException(String.format(
					"Unnable to find version attribute of type: %s, @Version not found", type.getJavaType().getName()));
		}

	}

	protected interface Getter {

		Object get(Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

		Member getMember();

		Method getMethod();

		String getMethodName();

		Class<?> getReturnType();

	}

	protected class GetterMethod implements Getter {

		private final Method method;

		private final Member member;

		public GetterMethod(Method method) {
			super();
			this.method = method;
			this.member = method;
		}

		@Override
		public Object get(Object object)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			// TODO Auto-generated method stub
			return method.invoke(object);
		}

		@Override
		public Member getMember() {
			// TODO Auto-generated method stub
			return member;
		}

		@Override
		public Method getMethod() {
			// TODO Auto-generated method stub
			return method;
		}

		@Override
		public String getMethodName() {
			// TODO Auto-generated method stub
			return method.getName();
		}

		@Override
		public Class<?> getReturnType() {
			// TODO Auto-generated method stub
			return method.getReturnType();
		}

	}

	protected interface Setter {

		void set(Object arg, Object object)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

		Method getMethod();

		String getMethodName();

	}

	protected class SetterMethod implements Setter {

		private final Method method;

		/**
		 * 
		 */
		public SetterMethod(Method method) {
			// TODO Auto-generated constructor stub
			this.method = method;
		}

		@Override
		public void set(Object arg, Object object)
				throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			// TODO Auto-generated method stub
			method.invoke(object, arg);
		}

		@Override
		public Method getMethod() {
			// TODO Auto-generated method stub
			return method;
		}

		@Override
		public String getMethodName() {
			// TODO Auto-generated method stub
			return method.getName();
		}

	}

}
