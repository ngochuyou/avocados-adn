/**
 * 
 */
package adn.service.resource;

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

	private final MutableIdentifierGeneratorFactory mutableIdentifierGeneratorFactory;
	private final BasicTypeRegistry typeRegistry;

	public SharedIdentifierGeneratorFactory(BasicTypeRegistry typeRegistry,
			MutableIdentifierGeneratorFactory mutableIdentifierGeneratorFactory) {
		this.typeRegistry = typeRegistry;
		this.mutableIdentifierGeneratorFactory = mutableIdentifierGeneratorFactory;
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
		generatorMap.put(name, mutableIdentifierGeneratorFactory.createIdentifierGenerator(name, type, props));

		return generatorMap.get(name);
	}

	@Override
	public Class<?> getIdentifierGeneratorClass(String strategy) {
		// TODO Auto-generated method stub
		return mutableIdentifierGeneratorFactory.getIdentifierGeneratorClass(strategy);
	}

	@Override
	@Deprecated
	public Dialect getDialect() {
		return null;
	}

	@Override
	@Deprecated
	public void setDialect(Dialect dialect) {}

}
