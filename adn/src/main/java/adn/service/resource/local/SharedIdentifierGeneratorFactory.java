/**
 * 
 */
package adn.service.resource.local;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.service.Service;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * @author Ngoc Huy
 *
 */
@SuppressWarnings("serial")
public class SharedIdentifierGeneratorFactory implements IdentifierGeneratorFactory, Service {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, IdentifierGenerator> generatorMap = new HashMap<>();

	private final ContextBuildingService contextService;
	private final BasicTypeRegistry typeRegistry;

	public SharedIdentifierGeneratorFactory(ContextBuildingService contextService) {
		this.contextService = contextService;
		typeRegistry = contextService.getServiceWrapper(BasicTypeRegistry.class,
				wrapper -> wrapper.orElseThrow().unwrap());
	}

	public <X extends IdentifierGenerator> IdentifierGenerator obtainGenerator(String name) {
		if (!generatorMap.containsKey(name)) {
			return null;
		}

		return generatorMap.get(name);
	}

	public IdentifierGenerator createIdentifierGenerator(String name, Class<?> identifierJavaType) {

		return createIdentifierGenerator(name, typeRegistry.getRegisteredType(identifierJavaType.getName()), null);
	}

	@Override
	public IdentifierGenerator createIdentifierGenerator(String name, Type type, Properties config) {
		Assert.notNull(name, "IdentifierGenerator name must not be null");
		Assert.notNull(type, "Identifier type must not be null");

		if (generatorMap.containsKey(name)) {
			logger.trace("Ignoring registration of IdentifierGenerator of name " + name);
			return generatorMap.get(name);
		}

		logger.trace("Creating new IdentifierGenerator of name " + name);

		Properties props = new Properties();

		props.setProperty(IdentifierGenerator.GENERATOR_NAME, name);
		generatorMap.put(name, contextService.getService(MutableIdentifierGeneratorFactory.class)
				.createIdentifierGenerator(name, type, props));

		return generatorMap.get(name);
	}

	@Override
	public Class<?> getIdentifierGeneratorClass(String strategy) {
		// TODO Auto-generated method stub
		return contextService.getService(MutableIdentifierGeneratorFactory.class).getIdentifierGeneratorClass(strategy);
	}

	@Override
	@Deprecated
	public Dialect getDialect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void setDialect(Dialect dialect) {
		// TODO Auto-generated method stub

	}

}
