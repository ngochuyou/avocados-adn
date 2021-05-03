/**
 * 
 */
package adn.service.resource.metamodel;

import java.lang.reflect.Field;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import adn.service.resource.local.ManagerFactoryEventListener;
import adn.service.resource.local.SharedIdentifierGeneratorFactory;
import adn.service.resource.local.factory.EntityManagerFactoryImplementor;

/**
 * @author Ngoc Huy
 *
 */
public class EntityBinder implements ManagerFactoryEventListener {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static EntityBinder INSTANCE;
	private final SharedIdentifierGeneratorFactory identifierGeneratorfactory;

	public EntityBinder(SharedIdentifierGeneratorFactory identifierGeneratorfactory) {
		Assert.notNull(identifierGeneratorfactory, SharedIdentifierGeneratorFactory.class + " must not be null");

		this.identifierGeneratorfactory = identifierGeneratorfactory;

		listen();
	}

	@Override
	public void postBuild(EntityManagerFactoryImplementor sessionFactory) {
		// TODO Auto-generated method stub
		logger.trace("Cleaning up INSTANCE of type " + this.getClass().getName());
		EntityBinder.INSTANCE = null;
		logger = null;
	}

	public <X, T> IdentifierGenerator locateIdentifierGenerator(ResourceType<X> metamodel,
			EntityManagerFactoryImplementor sessionFactory) throws IllegalAccessException {
		return locateIdentifierGenerator(metamodel, metamodel.getIdType().getJavaType(), sessionFactory);
	}

	private <X, T> IdentifierGenerator locateIdentifierGenerator(ResourceType<X> metamodel, Class<T> identifierType,
			EntityManagerFactoryImplementor sessionFactory) throws IllegalAccessException {
		Assert.notNull(identifierType, "Identifier type must not be null");

		SingularPersistentAttribute<X, T> idAttribute = metamodel.getDeclaredId(identifierType);
		Field idField = ((Field) idAttribute.getJavaMember());
		GenericGenerator ggAnno = idField.getDeclaredAnnotation(GenericGenerator.class);

		Assert.notNull(ggAnno,
				"Unable to locate @GenericGenerator field " + metamodel.getJavaType() + "." + idField.getName());

		String generatorName = ggAnno.strategy();

		Assert.hasLength(generatorName, "Invalid IdentifierGenrator name");

		IdentifierGenerator generator = identifierGeneratorfactory.obtainGenerator(generatorName);

		generator = (generator != null ? generator
				: identifierGeneratorfactory.createIdentifierGenerator(generatorName, idAttribute.getJavaType()));
		Assert.notNull(generator, "Unable to locate IdentifierGenrator for " + idAttribute.getName());

		return generator;
	}

}
