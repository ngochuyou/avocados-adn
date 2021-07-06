/**
 * 
 */
package adn.model.factory.property.production.authentication;

import static adn.helpers.LoggerHelper.with;
import static adn.helpers.StringHelper.get;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adn.application.context.ContextProvider;
import adn.helpers.TypeHelper;
import adn.model.AbstractModel;
import adn.model.ModelContextProvider;
import adn.model.entities.metadata.EntityMetadata;
import adn.model.factory.AuthenticationBasedModelPropertiesProducer;
import adn.model.factory.property.production.SecuredProperty;
import adn.service.internal.Role;

/**
 * @author Ngoc Huy
 *
 */
public class AuthenticationBasedModelPropertiesProducerImpl implements AuthenticationBasedModelPropertiesProducer {

	private final Map<Role, Map<String, Function<Object, Object>>> functionsMap;
	private final Map<Role, Map<String, String>> alternativeNamesMap;

	public <T extends AbstractModel> AuthenticationBasedModelPropertiesProducerImpl(Class<T> entityClass,
			Set<SecuredProperty<T>> securedProperties) {
		final Logger logger = LoggerFactory.getLogger(this.getClass());
		EntityMetadata metadata = ContextProvider.getBean(ModelContextProvider.class).getMetadata(entityClass);
		Map<Role, Map<String, Function<Object, Object>>> functionsMap = new HashMap<>(0, 1f);
		Map<Role, Map<String, String>> alternativeNamesMap = new HashMap<>(0, 1f);
		// @formatter:off
		securedProperties.stream().filter(prop -> {
			if (!TypeHelper.isParentOf(prop.getEntityType(), entityClass)
					|| !metadata.hasAttribute(prop.getPropertyName()) || prop.getFunction() == null) {
				logger.info(String.format("[%s]: Ignoring %s [%s] %s", entityClass.getName(),
						SecuredProperty.class.getSimpleName(), prop.getPropertyName(),
						!TypeHelper.isParentOf(prop.getEntityType(), entityClass) ? "invalid type"
								: !metadata.hasAttribute(prop.getPropertyName()) ? "unknown property"
										: " null function"));
				return false;
			}

			return true;
		}).sorted((o, t) -> o.getEntityType().equals(t.getEntityType()) ? 0
				: (TypeHelper.isExtendedFrom(o.getEntityType(), t.getEntityType()) ? 1 : -1)
		).forEach(prop -> {
			if (!functionsMap.containsKey(prop.getRole())) {
				functionsMap.put(prop.getRole(),
						Stream.of(Map.entry(prop.getPropertyName(), prop.getFunction()))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				alternativeNamesMap.put(prop.getRole(),
						Stream.of(Map.entry(prop.getPropertyName(),
								get(prop.getPropertyAlternativeName()).orElse(prop.getPropertyName())))
								.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
				return;
			}

			Map<String, Function<Object, Object>> functions = functionsMap.get(prop.getRole());
			Map<String, String> alternativeNames = alternativeNamesMap.get(prop.getRole());

			if (functions.containsKey(prop.getPropertyName())) {
				logger.info(String.format("Overriding function of property [%s]", prop.getPropertyName()));
				functions.put(prop.getPropertyName(), prop.getFunction());
				logger.info(String.format("Overriding alternative name [%s] for property [%s]",
						prop.getPropertyAlternativeName(), prop.getPropertyName()));
				alternativeNames.put(prop.getPropertyName(), prop.getPropertyAlternativeName());
				return;
			}

			functions.put(prop.getPropertyName(), prop.getFunction());
			alternativeNames.put(prop.getPropertyName(),
					get(prop.getPropertyAlternativeName()).orElse(prop.getPropertyName()));
		});
		functionsMap.entrySet().forEach(entry -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				entry.getValue().computeIfAbsent(propName,
						key -> with(logger)
								.trace(String.format("[%s#%s]: Masking property [%s]", entityClass.getName(), entry.getKey(), propName),
										MASKER));
			});
		});
		alternativeNamesMap.values().forEach(propNames -> {
			metadata.getPropertyNames().stream().forEach(propName -> {
				propNames.computeIfAbsent(propName, key -> propName);
				propNames.merge(propName, propName, (o, n) -> Optional.ofNullable(o).orElse(n));
			});
		});

		Stream.of(Role.values()).forEach(role -> {
			functionsMap.computeIfAbsent(role, key -> {
				return metadata.getPropertyNames().stream()
						.map(prop -> Map.entry(prop, with(logger)
														.trace(String.format("[%s#%s]: Masking property [%s]", entityClass.getName(), role, prop),
																MASKER)))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			});
			alternativeNamesMap.computeIfAbsent(role, key -> {
				return metadata.getPropertyNames().stream()
						.map(prop -> Map.entry(prop, prop))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			});
		});
		// @formatter:on
		if (!metadata.getPropertyNames().isEmpty()) {
			functionsMap.put(null, metadata.getPropertyNames().stream().map(prop -> Map.entry(prop, MASKER))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
			alternativeNamesMap.put(null, metadata.getPropertyNames().stream().map(prop -> Map.entry(prop, prop))
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
		}

		this.functionsMap = Collections.unmodifiableMap(functionsMap);
		this.alternativeNamesMap = Collections.unmodifiableMap(alternativeNamesMap);

		LoggerFactory.getLogger(this.getClass()).trace(String.format("\n" + "%s:\n" + "\t%s", entityClass.getName(),
				functionsMap.entrySet().stream().map(entry -> entry.getValue().entrySet().stream()
						.map(fEntry -> String.format("%s:\t[%s] -> [%s]", entry.getKey(),
								this.alternativeNamesMap.get(entry.getKey()).get(fEntry.getKey()),
								fEntry.getValue().equals(MASKER) ? "masked"
										: fEntry.getValue().equals(PUBLISHER) ? "published" : fEntry.getValue()))
						.collect(Collectors.joining("\n\t"))).collect(Collectors.joining("\n\t"))));
	}

	@Override
	public Map<String, Object> produce(Map<String, Object> source, Role role) {
		Map<String, Function<Object, Object>> functionsByRole = functionsMap.get(role);
		Map<String, String> alternativeNamesByRole = alternativeNamesMap.get(role);

		return source.entrySet().stream()
				.map(entry -> produceRow(entry.getKey(), entry.getValue(), functionsByRole, alternativeNamesByRole))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	@Override
	public List<Map<String, Object>> produce(List<Map<String, Object>> sources, Role role) {
		Map<String, Function<Object, Object>> functionsByRole = functionsMap.get(role);
		Map<String, String> alternativeNamesByRole = alternativeNamesMap.get(role);

		return sources.stream()
				.map(source -> source.entrySet().stream().map(
						entry -> produceRow(entry.getKey(), entry.getValue(), functionsByRole, alternativeNamesByRole))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
				.collect(Collectors.toList());
	}

	private Map.Entry<String, Object> produceRow(String originalPropName, Object value,
			Map<String, Function<Object, Object>> functionsByRole, Map<String, String> alternativeNamesByRole) {
		try {
			return Map.entry(alternativeNamesByRole.get(originalPropName),
					functionsByRole.get(originalPropName).apply(value));
		} catch (NullPointerException npe) {
			npe.printStackTrace();
			// This could be caused when passing properties in which are not contained in
			// this producer. Seriously though, this should be avoided by devs
			return Map.<String, Object>entry(alternativeNamesByRole.get(originalPropName), value);
		} catch (Exception any) {
			any.printStackTrace(); // should be avoided
			return Map.<String, Object>entry(alternativeNamesByRole.get(originalPropName), value);
		}
	}

}
