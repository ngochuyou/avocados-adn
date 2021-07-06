/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.property.access.spi.Getter;

import adn.model.AbstractModel;
import adn.model.entities.metadata.EntityMetadata;

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

	public DefaultModelProducer(Class<T> type, EntityMetadata metadata) {
		getters = metadata.getGetters();
		name = String.format("%s<%s>", this.getClass().getSimpleName(), type.getName());
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
