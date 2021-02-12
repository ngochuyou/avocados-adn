/**
 * 
 */
package adn.service.resource.tuple;

import javax.persistence.metamodel.Attribute;

import org.springframework.boot.SpringApplication;

import adn.application.context.ContextProvider;
import adn.service.resource.metamodel.EntityMode;
import adn.service.resource.metamodel.ResourceMetamodel;

/**
 * @author Ngoc Huy
 *
 */
public class PojoResourceTuplizer extends AbstractResourceTuplizer {

	/**
	 * @param metamodel
	 */
	public PojoResourceTuplizer(ResourceMetamodel metamodel) {
		super(metamodel);
		// TODO Auto-generated constructor stub
	}

	@Override
	public EntityMode getEntityMode() {
		// TODO Auto-generated method stub
		return EntityMode.POJO;
	}

	@Override
	protected <X, Y> Getter buildPropertyGetter(Attribute<X, Y> attribute) {
		// TODO Auto-generated method stub
		try {
			return PropertyAccessBuildingStrategy.CAMEL_CASED_INSTANCE.buildGetter(attribute);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
			return null;
		}
	}

	@Override
	protected <X, Y> Setter buildPropertySetter(Attribute<X, Y> attribute) {
		// TODO Auto-generated method stub
		try {
			return PropertyAccessBuildingStrategy.CAMEL_CASED_INSTANCE.buildSetter(attribute);
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
			return null;
		}
	}

	@Override
	protected Instantiator buildInstantiator(ResourceMetamodel metamodel) {
		// TODO Auto-generated method stub
		try {
			return new PlainInstanceInstantiator(metamodel.getMappedClass(), metamodel.getIdentifierAttribute().isEmbedded(), metamodel.isAbstract());
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
			return null;
		}
	}

	@Override
	public Class<?> getMappedClass() {
		// TODO Auto-generated method stub
		return getMetamodel().getMappedClass();
	}

}
