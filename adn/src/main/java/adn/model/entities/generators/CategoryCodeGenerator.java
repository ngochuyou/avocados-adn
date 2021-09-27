/**
 * 
 */
package adn.model.entities.generators;

import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

import adn.model.Generic;
import adn.model.entities.Category;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Category.class)
public class CategoryCodeGenerator implements ValueGenerator<String> {

	public static final String NAME = "CategoryIdGenerator";
	public static final String PATH = "adn.model.entities.generators.CategoryIdGenerator";

	@Override
	public String generateValue(Session session, Object owner) {
		return UUID.randomUUID().toString();
	}

//	@Override
//	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {}
//
//	@Override
//	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
//		Category category = (Category) object;
//		String name;
//
//		Assert.isTrue(StringHelper.hasLength(name = String.valueOf(category.getName())), "Category name was null");
//
//		name = StringHelper.removeSpaces(name);
//
//		String code = name.length() >= _Category.IDENTIFIER_LENGTH
//				? name.substring(0, _Category.IDENTIFIER_LENGTH).toUpperCase()
//				: RandomStringUtils.randomAlphanumeric(_Category.IDENTIFIER_LENGTH).toUpperCase();
//
//		return code;
//	}

}
