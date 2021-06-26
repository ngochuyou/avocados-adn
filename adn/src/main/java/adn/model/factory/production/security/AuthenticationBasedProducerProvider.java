package adn.model.factory.production.security;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.AbstractModel;
import adn.model.Generic;
import adn.model.ModelsDescriptor;
import adn.model.entities.Entity;
import adn.model.factory.ModelProducerProvider;
import adn.model.models.Model;
import adn.service.internal.Role;

@Component(AuthenticationBasedProducerProvider.NAME)
@Order(value = 5)
public class AuthenticationBasedProducerProvider implements ModelProducerProvider {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String NAME = "authenticationBasedProducerProvider";
	private final String MODEL_PRODUCER_PACKAGE = "adn.model.factory.production.security";

	private Map<Class<? extends AbstractModel>, AuthenticationBasedModelProducer<?, ?>> producerMap;
	private AuthenticationBasedModelProducer<Entity, AbstractModel> defaultProducer = new AuthenticationBasedModelProducer<Entity, AbstractModel>() {

		@Override
		public AbstractModel produceForAdminAuthentication(Entity entity, AbstractModel model) {
			return entity;
		}

		@Override
		public AbstractModel produceForPersonnelAuthentication(Entity entity, AbstractModel model) {
			return entity;
		};

		@Override
		public AbstractModel produceForCustomerAuthentication(Entity entity, AbstractModel model) {
			return entity;
		};

		@Override
		public AbstractModel produceForAnonymous(Entity entity, AbstractModel model) {
			return entity;
		}

	};
	private Map<Role, BiFunction<Entity, Class<Model>, ? extends AbstractModel>> functionMap;

	@Autowired
	private ModelsDescriptor modelsDescriptor;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		logger.info(getLoggingPrefix(this) + "Initializing " + this.getClass());
		this.producerMap = new HashMap<>();
		// @formatter:off
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AnnotationTypeFilter(Generic.class));
		scanner.findCandidateComponents(MODEL_PRODUCER_PACKAGE).forEach(bean -> {
			try {
				Class<? extends AuthenticationBasedModelProducer<?, ?>> clazz = (Class<? extends AuthenticationBasedModelProducer<?, ?>>) Class
						.forName(bean.getBeanClassName());
				Generic anno = clazz.getDeclaredAnnotation(Generic.class);

				this.producerMap.put(anno.modelGene(), ContextProvider.getApplicationContext().getBean(clazz));
			} catch (Exception e) {
				e.printStackTrace();
				SpringApplication.exit(ContextProvider.getApplicationContext());
			}
		});
		modelsDescriptor.getRelationMap().values().forEach(set -> set.forEach(clazz -> {
			if (this.producerMap.get(clazz) == null) {
				this.producerMap.put(clazz, defaultProducer);
			}
		}));
		modelsDescriptor.getModelTree().forEach(node -> {
			if (node.getParent() == null) {
				return;
			}

			AuthenticationBasedModelProducer<?, ?> comp = null;
			AuthenticationBasedModelProducer<?, ?> parent = this.producerMap.get(node.getParent().getNode());
			AuthenticationBasedModelProducer<?, ?> child = this.producerMap.get(node.getNode());

			if (parent != null && child != null) {
				if (!parent.equals(defaultProducer) && !child.equals(defaultProducer)) {
					comp = parent.and(child);
				} else {
					comp = !parent.equals(defaultProducer) ? parent : child;
				}
			} else {
				if (parent != null) {
					comp = !parent.equals(defaultProducer) ? parent : null;
				} else {
					comp = !child.equals(defaultProducer) ? child : null;
				}
			}

			this.producerMap.put(node.getNode(), comp == null ? defaultProducer : comp);
		});
		this.producerMap.forEach((k, v) -> {
			logger.info(String.format("[%s] -> [%s]", v.getClass().getName(), k.getName()));
		});
		this.functionMap = new HashMap<>();
		this.functionMap.put(Role.ADMIN, this::produceForAdmin);
		this.functionMap.put(Role.CUSTOMER, this::produceForCustomer);
		this.functionMap.put(Role.PERSONNEL, this::produceForPersonnel);
		this.functionMap.put(Role.ANONYMOUS, this::produceForAnonymous);
		this.functionMap.put(null, this::produceForAnonymous);
		// @formatter:on
		logger.info(getLoggingPrefix(this) + "Finished initializing " + this.getClass());
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity, M extends AbstractModel> AuthenticationBasedModelProducer<T, M> getProducer(
			Class<M> modelClass) {
		return (AuthenticationBasedModelProducer<T, M>) this.producerMap.get(modelClass);
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, M extends AbstractModel> M produceForAdmin(T entity, Class<M> clazz) {
		try {
			AuthenticationBasedModelProducer<T, M> producer = (AuthenticationBasedModelProducer<T, M>) this.producerMap
					.get(clazz);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Producing model of entity [%s] using [%s] for ADMIN",
						entity.getClass().getName(), producer.getName()));
			}

			return produce(producer, clazz, entity, producer::produceForAdminAuthentication);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException
				| NoSuchMethodException re) {
			re.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, M extends AbstractModel> M produceForCustomer(T entity, Class<M> clazz) {
		try {
			AuthenticationBasedModelProducer<T, M> producer = (AuthenticationBasedModelProducer<T, M>) this.producerMap
					.get(clazz);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Producing model of entity [%s] using [%s] for CUSTOMER",
						entity.getClass().getName(), producer.getName()));
			}

			return produce(producer, clazz, entity, producer::produceForCustomerAuthentication);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException
				| NoSuchMethodException re) {
			re.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, M extends AbstractModel> M produceForPersonnel(T entity, Class<M> clazz) {
		try {
			AuthenticationBasedModelProducer<T, M> producer = (AuthenticationBasedModelProducer<T, M>) this.producerMap
					.get(clazz);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Producing model of entity [%s] using [%s] for PERSONNEL",
						entity.getClass().getName(), producer.getName()));
			}

			return produce(producer, clazz, entity, producer::produceForPersonnelAuthentication);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends Entity, M extends AbstractModel> M produceForAnonymous(T entity, Class<M> clazz) {
		try {
			AuthenticationBasedModelProducer<T, M> producer = (AuthenticationBasedModelProducer<T, M>) this.producerMap
					.get(clazz);

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Producing model of entity [%s] using [%s] for PERSONNEL",
						entity.getClass().getName(), producer.getName()));
			}

			return produce(producer, clazz, entity, producer::produceForAnonymous);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity, M extends AbstractModel> M produce(T entity, Class<M> clazz, Role role) {
		return (M) this.functionMap.get(role).apply(entity, (Class<Model>) clazz);
	}

	private <T extends Entity, M extends AbstractModel> M produce(AuthenticationBasedModelProducer<T, M> producer,
			Class<M> modelType, T entity, BiFunction<T, M, M> fnc)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		return producer.equals(defaultProducer) ? fnc.apply(entity, null)
				: fnc.apply(entity, TypeHelper.newModelOrAbstract(modelType));
	}

}
