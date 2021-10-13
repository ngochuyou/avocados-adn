/**
 * 
 */
package adn.application.context.builders;

import static adn.application.Common.notEmpty;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
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
import adn.application.Result;
import adn.application.context.ContextProvider;
import adn.application.context.internal.ContextBuilder;
import adn.dao.generic.GenericRepository;
import adn.helpers.HibernateHelper;
import adn.helpers.StringHelper;
import adn.helpers.TypeHelper;
import adn.model.Generic;
import adn.model.entities.Entity;
import adn.model.entities.NamedResource;
import adn.model.entities.SpannedResource;
import adn.model.entities.metadata._NamedResource;
import adn.model.entities.metadata._SpannedResource;
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
			return Result.ok(instance);
		}

		@Override
		public Result<Entity> isSatisfiedBy(Session session, Serializable id, Entity instance) {
			return Result.ok(instance);
		}

		@Override
		public String getLoggableName() {
			return NAME;
		}

	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
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

		final Map<Class, Validator> fixedValidators = Map.of(
				NamedResource.class, new NamedResourceValidator(ContextProvider.getBean(GenericRepository.class)),
				SpannedResource.class, new SpannedResourceValidator());
		
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
				
				for (Class<?> interfaceType: ClassUtils.getAllInterfacesForClassAsSet(type)) {
					if (fixedValidators.containsKey(interfaceType)) {
						if (!validator.equals(DEFAULT_VALIDATOR)) {
							validatorMap.put(type, fixedValidators.get(interfaceType).and(validator));
							return;
						}
						
						validatorMap.put(type, fixedValidators.get(interfaceType));
					}
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
					String.format("^[%s\\p{L}\\p{N}\\s\\.,_\\-@\"\'%%*#+]{%d,%d}$", StringHelper.VIETNAMESE_CHARACTERS,
							_NamedResource.MINIMUM_NAME_LENGTH, _NamedResource.MAXIMUM_NAME_LENGTH));
		}

		private static final String INVALID_NAME = String.format(
				"Name can only contain alphabetic, numeric, %s characters and %s",
				Common.symbolNamesOf('\s', '.', ',', '_', '-', '@', '\"', '\'', '%', '*', '+', '#'),
				Common.hasLength(null, _NamedResource.MINIMUM_NAME_LENGTH, _NamedResource.MAXIMUM_NAME_LENGTH));
		private static final String TAKEN_NAME = "Name was taken";

		private final GenericRepository genericRepository;

		private NamedResourceValidator(GenericRepository genericRepository) {
			super();
			this.genericRepository = genericRepository;
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
			return Result.ok(instance);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static class SpannedResourceValidator extends AbstractCompositeEntityValidator {

		private static final String UNSUPPORTED_TEMPORAL_TYPE_TEMPLATE = String.format("Unsupported %s type: [%s]",
				Temporal.class.getSimpleName(), "%s");

		private static final String MISSING_APPLIED_TIMESTAMP = notEmpty("Applied timestamp");
		private static final String MISSING_DROPPED_TIMESTAMP = notEmpty("Dropped timestamp");
		private static final String INVALID_TIMESTAMP = Common.sequential("Applied timestamp", "dropped timestamp");
		// @formatter:off
		private static final Map<Class<? extends Temporal>, Class<? extends Temporal>> CONSUMER_KEY_RESOLVERS = Map.of(
				LocalDateTime.class, ChronoLocalDateTime.class,
				LocalDate.class, ChronoLocalDate.class,
				LocalTime.class, LocalTime.class);
		private static final Map<Class<? extends Temporal>, SpanValidator> CONSUMERS;
		
		static {
			Map<Class<? extends Temporal>, SpanValidator> consumers = new HashMap<>(0, 1f);
			
			consumers.put(ChronoLocalDateTime.class, (temporalType, resource, result, resultConsumer) -> {
				ChronoLocalDateTime appliedTimestamp = (ChronoLocalDateTime) resource.getAppliedTimestamp();
				ChronoLocalDateTime droppedTimestamp = (ChronoLocalDateTime) resource.getDroppedTimestamp();

				if (appliedTimestamp.isAfter(droppedTimestamp)) {
					resultConsumer.accept(result);
				}
			});
			consumers.put(ChronoLocalDate.class, (temporalType, resource, result, resultConsumer) -> {
				ChronoLocalDate appliedTimestamp = (ChronoLocalDate) resource.getAppliedTimestamp();
				ChronoLocalDate droppedTimestamp = (ChronoLocalDate) resource.getDroppedTimestamp();

				if (appliedTimestamp.isAfter(droppedTimestamp)) {
					resultConsumer.accept(result);
				}
			});
			consumers.put(LocalTime.class, (temporalType, resource, result, resultConsumer) -> {
				LocalTime appliedTimestamp = (LocalTime) resource.getAppliedTimestamp();
				LocalTime droppedTimestamp = (LocalTime) resource.getDroppedTimestamp();

				if (appliedTimestamp.isAfter(droppedTimestamp)) {
					resultConsumer.accept(result);
				}
			});
			consumers.put(null, (temporalType, resource, result, resultConsumer) -> {
				throw new IllegalArgumentException(String.format(UNSUPPORTED_TEMPORAL_TYPE_TEMPLATE, temporalType.getName()));
			});
			
			CONSUMERS = Collections.unmodifiableMap(consumers);
		}
		// @formatter:on
		@Override
		public Result isSatisfiedBy(Session session, Serializable id, Entity instance) {
			SpannedResource resource = (SpannedResource) instance;
			Result result = Result.ok(instance);

			boolean hasAppliedTimestamp = resource.getAppliedTimestamp() != null;

			if (!hasAppliedTimestamp) {
				result.bad(_SpannedResource.appliedTimestamp, MISSING_APPLIED_TIMESTAMP);
			}

			boolean hasDroppedTimestamp = resource.getDroppedTimestamp() != null;

			if (!hasDroppedTimestamp) {
				result.bad(_SpannedResource.droppedTimestamp, MISSING_DROPPED_TIMESTAMP);
			}

			if (hasAppliedTimestamp && hasDroppedTimestamp) {
				assertSpan(resource, result);
			}

			return result;
		}

		private void assertSpan(SpannedResource resource, Result result) {
			Class<? extends Temporal> temporalType = resource.getAppliedTimestamp().getClass();

			CONSUMERS.get(CONSUMER_KEY_RESOLVERS.get(temporalType)).validate(temporalType, resource, result,
					this::makeResultBad);
		}

		private void makeResultBad(Result result) {
			result.bad(_SpannedResource.droppedTimestamp, INVALID_TIMESTAMP);
			result.bad(_SpannedResource.appliedTimestamp, INVALID_TIMESTAMP);
		}

		private interface SpanValidator {

			void validate(Class<? extends Temporal> temporalType, SpannedResource source, Result result,
					Consumer<Result> resultConsumer);

		}

	}

}
