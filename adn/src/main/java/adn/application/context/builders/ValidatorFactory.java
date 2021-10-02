/**
 * 
 */
package adn.application.context.builders;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import adn.application.Common;
import adn.application.Constants;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.dao.generic.GenericRepository;
import adn.dao.generic.Result;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.entities.NamedResource;
import adn.model.entities.metadata._NamedResource;
import adn.model.entities.validator.AbstractCompositeEntityValidator;
import adn.model.entities.validator.Validator;

/**
 * @author Ngoc Huy
 *
 */
@Component
public class ValidatorFactory implements ContextBuilder {

	private Map<Class<? extends Entity>, Validator<?>> validatorMap;
	private static final Validator<Entity> DEFAULT_VALIDATOR = new AbstractCompositeEntityValidator<>() {

		private static final String NAME = "DEFAULT_VALIDATOR";
		
		@Override
		public Result<Entity> isSatisfiedBy(Session session, Entity instance) {
			return Result.success(instance);
		}

		@Override
		public Result<Entity> isSatisfiedBy(Session session, Serializable id, Entity instance) {
			return Result.success(instance);
		}

		@Override
		public String getLoggableName() {
			return NAME;
		}

	};

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() {
		Logger logger = LoggerFactory.getLogger(ValidatorFactory.class);

		logger.info("Building " + this.getClass().getName());

		Map<Class<? extends Entity>, Validator<?>> validatorMap = new HashMap<>();
		ModelContextProvider modelManager = ContextProvider.getBean(ModelContextProvider.class);
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		// @formatter:off
		scanner.addIncludeFilter(new AssignableTypeFilter(Validator.class));

		try {
			for (BeanDefinition beanDef : scanner.findCandidateComponents(Constants.VALIDATOR_PACKAGE)) {
				Class<? extends Validator<?>> clazz = (Class<? extends Validator<?>>) Class.forName(beanDef.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);
				
				if (!Entity.class.isAssignableFrom(anno.entityGene())) {
					continue;
				}
				
				validatorMap.put((Class<? extends Entity>) anno.entityGene(),
						(Validator<?>) ContextProvider.getApplicationContext()
							.getBean(TypeHelper.getComponentName(clazz)));
			}
		} catch (Exception e) {
			e.printStackTrace();
			SpringApplication.exit(ContextProvider.getApplicationContext());
		}

		final NamedResourceValidator namedResourceValidator = new NamedResourceValidator(ContextProvider.getBean(GenericRepository.class));
		
		modelManager.getEntityTree()
			.forEach(branch -> {
				Class<? extends Entity> type = (Class<? extends Entity>) branch.getNode();
				
				_try: try {
					if (validatorMap.get(type) == null) {
						if (branch.getParent() == null) {
							validatorMap.put(type, DEFAULT_VALIDATOR);
							break _try;
						}
						
						Validator<?> parentSpec = validatorMap.get(branch.getParent().getNode());
						
						validatorMap.put(type, parentSpec != null ? parentSpec : DEFAULT_VALIDATOR);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				Validator<?> validator = validatorMap.get(type);

				if (ClassUtils.getAllInterfacesForClassAsSet(type).contains(NamedResource.class)) {
					if (!validator.equals(DEFAULT_VALIDATOR)) {
						validatorMap.put(type, namedResourceValidator.and(validator));
						return;
					}
					
					validatorMap.put(type, namedResourceValidator);
				}
			});
		validatorMap.forEach((k, v) -> logger.debug(String.format("Registered %s for [%s] ", v.getLoggableName(), k.getName())));
		// @formatter:on
		this.validatorMap = Collections.unmodifiableMap(validatorMap);

		logger.info("Finished building" + this.getClass().getName());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> Validator<T> getValidator(Class<T> clazz) {
		return (Validator<T>) this.validatorMap.get(clazz);
	}

	@SuppressWarnings("rawtypes")
	private static class NamedResourceValidator extends AbstractCompositeEntityValidator {

		private static final Pattern NAME_PATTERN;

		static {
			NAME_PATTERN = Pattern.compile(
					String.format("^[%s\\p{L}\\p{N}\\s\\.,_\\-@\"\'%%*]{%d,%d}$", StringHelper.VIETNAMESE_CHARACTERS,
							_NamedResource.MINIMUM_NAME_LENGTH, _NamedResource.MAXIMUM_NAME_LENGTH));
		}

		private static final String INVALID_NAME = String.format(
				"Name length must vary between %d and %d. Can only contain alphabetic, numeric characters, %s",
				_NamedResource.MINIMUM_NAME_LENGTH, _NamedResource.MAXIMUM_NAME_LENGTH,
				Common.symbolNamesOf('\s', '.', ',', '_', '-', '@', '\"', '\'', '%', '*'));
		private static final String TAKEN_NAME = "Name was taken";

		private final GenericRepository genericRepository;

		private NamedResourceValidator(GenericRepository genericRepository) {
			super();
			this.genericRepository = genericRepository;
		}

		@Override
		public Result isSatisfiedBy(Session session, Entity instance) {
			return isSatisfiedBy(session, HibernateHelper.getIdentifier(instance), instance);
		}

		@Override
		public Result isSatisfiedBy(Session session, Serializable id, Entity instance) {
			NamedResource resource = (NamedResource) instance;

			if (!NAME_PATTERN.matcher(resource.getName()).matches()) {
				return Result.bad(Map.of(_NamedResource.name, INVALID_NAME));
			}

			Class<Entity> persistentClass = HibernateHelper.getPersistentClass(instance);
			String idPropertyName = HibernateHelper.getIdentifierPropertyName(persistentClass);
			// @formatter:off
			if (genericRepository.count(persistentClass,
					(root, query, builder) -> builder.and(
							id != null ?
								builder.notEqual(root.get(idPropertyName), id) :
									builder.isNotNull(root.get(idPropertyName)),
							builder.equal(root.get(_NamedResource.name), resource.getName()))) != 0) {
				return Result.bad(Map.of(_NamedResource.name, TAKEN_NAME));
			}
			// @formatter:on
			return Result.success(instance);
		}

	}

}
