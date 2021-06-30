/**
 * 
 */
package adn.model.factory.production.security;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.MappingException;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.slf4j.LoggerFactory;

import adn.engine.access.StandardAccess;
import adn.helpers.EntityUtils;
import adn.model.AbstractModel;
import adn.model.entities.Entity;

/**
 * Literally get everything, mask nothing from a {@link AbstractModel} instance
 * 
 * @author Ngoc Huy
 *
 */
public class DefaultModelProducer<T extends AbstractModel>
		extends AbstractCompositeAuthenticationBasedModelProducerImplementor<T> {

	private final Set<Map.Entry<String, Getter>> getters;

	private final String name;

	public static <T extends AbstractModel> boolean shouldUse(Class<T> type) {
		return type.getDeclaredFields().length > 0;
	}

	@SuppressWarnings("unchecked")
	public DefaultModelProducer(Class<T> type) {
		this.getters = Entity.class.isAssignableFrom(type) ? locateFromHibernate((Class<? extends Entity>) type)
				: resolve(type);
		name = String.format("%s<%s>", this.getClass().getSimpleName(), type.getName());
	}

	/**
	 * For memory utilisation
	 */
	@SuppressWarnings("unchecked")
	private <E extends Entity> Set<Map.Entry<String, Getter>> locateFromHibernate(Class<E> type) {
		try {
			EntityMetamodel metamodel = EntityUtils.getEntityPersister(type).getEntityMetamodel();
			EntityTuplizer tuplizer = metamodel.getTuplizer();

			return Stream.of(metamodel.getPropertyNames())
					.map(name -> Map.entry(name, tuplizer.getGetter(metamodel.getPropertyIndex(name))))
					.collect(Collectors.toSet());
		} catch (MappingException e) {
			LoggerFactory.getLogger(this.getClass())
					.warn(String.format(
							"Unable to resolve getters of [%s] from Hibernate, resolving getters through %s",
							type.getName(), StandardAccess.class.getName()));
			return resolve((Class<T>) type);
		}
	}

	private Set<Map.Entry<String, Getter>> resolve(Class<T> type) {
		Field[] fields = type.getDeclaredFields();
		int propertySpan = fields.length;
		Map<String, Getter> accessors = new HashMap<>(propertySpan, 1.1f);

		for (Field f : fields) {
			accessors.put(f.getName(),
					StandardAccess.locateGetter(type, f.getName())
							.orElseThrow(() -> new IllegalArgumentException(
									String.format("Unable to locate getter for property [%s] in type [%]", f.getName(),
											type.getName()))));
		}

		return accessors.entrySet();
	}

	private Map<String, Object> getAll(T entity) {
		return getters.stream().map(entry -> Map.entry(entry.getKey(), entry.getValue().get(entity)))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private Map<String, Object> injectAll(T entity, Map<String, Object> model) {
		model.putAll(getAll(entity));

		return model;
	}

	@Override
	protected Map<String, Object> produceForAdmin(T entity, Map<String, Object> model) {
		return injectAll(entity, model);
	}

	@Override
	protected Map<String, Object> produceForPersonnel(T entity, Map<String, Object> model) {
		return injectAll(entity, model);
	}

	@Override
	protected Map<String, Object> produceForEmployee(T entity, Map<String, Object> model) {
		return injectAll(entity, model);
	}

	@Override
	protected Map<String, Object> produceForManager(T entity, Map<String, Object> model) {
		return injectAll(entity, model);
	}

	@Override
	protected Map<String, Object> produceForCustomer(T entity, Map<String, Object> model) {
		return injectAll(entity, model);
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("\n%s(\n]tgetters=[%s]\n)",
				this.getClass().getSimpleName(),
				getters
					.stream()
					.map(entry -> String.format("%s(%s)", entry.getValue().getMethodName(), entry.getValue().getMethod().getReturnType()))
					.collect(Collectors.joining(", ")));
		// @formatter:on
	}

	@Override
	public String getName() {
		return name;
	}

}
