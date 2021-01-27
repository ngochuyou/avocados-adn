/**
 * 
 */
package adn.service.resource.persistence.metamodel;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.PersistenceException;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Type;

import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
public class PojoResourceTuplizer implements ResourceTuplizer {

	protected final ResourceMetamodel metamodel;

	protected final Getter idGetter;
	protected final Setter idSetter;

	@SuppressWarnings("unchecked")
	public <T, X> PojoResourceTuplizer(ResourceMetamodel metamodel, Class<T> resourceClass, Class<X> identifierClass)
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

		String identifierName = entityType.getDeclaredId(identifierType.getJavaType()).getName();
		String methodName = "";
		Method getterMethod;

		try {
			getterMethod = entityType.getJavaType().getDeclaredMethod(methodName = "get"
					+ String.valueOf(identifierName.charAt(0)).toUpperCase() + identifierName.substring(1));
			idGetter = new GetterMethod(getterMethod);
		} catch (NoSuchMethodException nsme) {
			throw new NoSuchMethodException(
					String.format("Unnable to locate getter for identifier of type: %s, tried: %s",
							resourceClass.getName(), methodName));
		}

		Method setterMethod;

		try {
			setterMethod = entityType.getJavaType().getDeclaredMethod(methodName = "set"
					+ String.valueOf(identifierName.charAt(0)).toUpperCase() + identifierName.substring(1),
					identifierType.getJavaType());
			idSetter = new SetterMethod(setterMethod);
		} catch (NoSuchMethodException nsme) {
			throw new NoSuchMethodException(String.format(
					"Unnable to locate setter for identifier of type: %s, tried: %s with identifier type: %s",
					resourceClass.getName(), methodName, identifierType.getJavaType().getName()));
		}
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
		return false;
	}

	@Override
	public <T> Class<T> getMappedClass() {
		// TODO Auto-generated method stub
		return null;
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
		return 0;
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
