/**
 * 
 */
package adn.model.entities.generators;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.Assert;

import adn.application.context.ContextProvider;
import adn.application.context.internal.EffectivelyFinal;
import adn.helpers.StringHelper;
import adn.model.Generic;
import adn.model.entities.Category;
import adn.model.entities.metadata._Category;
import adn.service.DomainEntityServiceObserver;
import adn.service.services.CategoryService;

/**
 * @author Ngoc Huy
 *
 */
@Generic(entityGene = Category.class)
public class CategoryIdGenerator
		implements IdentifierGenerator, Configurable, EffectivelyFinal, DomainEntityServiceObserver<Category> {

	private static final String ID = UUID.randomUUID().toString();
	public static final String NAME = "CategoryIdGenerator";
	public static final String PATH = "adn.model.entities.generators.CategoryIdGenerator";

	private static volatile Set<String> existingIds;

	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {}

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
		Category category = (Category) object;
		String name;

		Assert.isTrue(StringHelper.hasLength(name = String.valueOf(category.getName())), "Category name was null");

		name = StringHelper.removeSpaces(name);

		String id = name.length() >= _Category.IDENTIFIER_LENGTH
				? name.substring(0, _Category.IDENTIFIER_LENGTH).toUpperCase()
				: RandomStringUtils.randomAlphanumeric(_Category.IDENTIFIER_LENGTH).toUpperCase();

		if (existingIds.contains(id)) {
			id = RandomStringUtils.randomAlphanumeric(_Category.IDENTIFIER_LENGTH).toUpperCase();
		}

		return id;
	}

	public synchronized void addExistingId(String id) throws DuplicateKeyException {
		if (existingIds.contains(id)) {
			throw new DuplicateKeyException(String.format("Identifer [%s] is already existed", id));
		}

		existingIds.add(id);
	}

	public boolean hasId(String id) {
		return existingIds.contains(id);
	}

	private Access access = new Access() {

		@Override
		public void close() {
			access = null;
		}

		@Override
		public void execute() throws Exception {
			final Logger logger = LoggerFactory.getLogger(this.getClass());

			logger.info(String.format("Configuring %s", this.getClass().getName()));

			SessionFactory sf = ContextProvider.getBean(SessionFactory.class);
			Session ss = sf.openSession();

			ss.setDefaultReadOnly(true);

			List<String> ids = ss.createQuery("SELECT c.id FROM Category c", String.class).getResultList();

			ss.close();
			existingIds = new HashSet<>(ids.size());
			existingIds.addAll(ids);

			if (!existingIds.isEmpty()) {
				logger.info(String.format("Added [%s] to identifier store",
						existingIds.stream().collect(Collectors.joining(", "))));
			}

			ContextProvider.getBean(CategoryService.class).register(CategoryIdGenerator.this);
		}
	};

	@Override
	public Access getAccess() throws IllegalAccessException {
		return access;
	}

	@Override
	public void doNotify() throws Exception {}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void notifyCreation(Category newInstance) {
		addExistingId(newInstance.getId());
	}

	@Override
	public void notifyUpdate(Category newState) {}

}
