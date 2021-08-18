/**
 * 
 */
package adn.model.factory.dictionary.production.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.property.access.spi.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.helpers.Utils;
import adn.model.DomainEntity;
import adn.model.entities.metadata.DomainEntityMetadata;

/**
 * Literally get everything, mask nothing from a {@link DomainEntity} instance
 * 
 * @author Ngoc Huy
 *
 */
public class DefaultModelProducer<T extends DomainEntity>
		extends AbstractCompositeAuthenticationBasedModelProducerImplementor<T> {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(DefaultModelProducer.class);
	private final String name;

	private final Set<Map.Entry<String, Getter>> getters;

	public static <T extends DomainEntity> boolean shouldUse(Class<T> type) {
		return type.getDeclaredFields().length > 0;
	}

	public DefaultModelProducer(Class<T> type, DomainEntityMetadata metadata) {
		getters = metadata.getGetters();
		name = String.format("%s<%s>", this.getClass().getSimpleName(), type.getName());
	}

	private Map<String, Object> getAll(T entity) {
		return getters.stream().map(entry -> Utils.Entry.entry(entry.getKey(), entry.getValue().get(entity))).collect(
				HashMap<String, Object>::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
				HashMap::putAll);
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
	protected Map<String, Object> produceForCustomer(T entity, Map<String, Object> model) {
		return injectAll(entity, model);
	}

	@Override
	public String toString() {
		// @formatter:off
		return String.format("\n%s(\n]tgetters=[%s]\n)",
				name,
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
