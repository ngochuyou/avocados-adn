/**
 * 
 */
package adn.application.context.builders;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import adn.application.Constants;
import adn.application.context.internal.ContextBuilder;
import adn.model.factory.authentication.Credential;
import adn.model.factory.authentication.EnumeratedCredential;
import adn.model.factory.authentication.OnMemoryCredential;
import adn.model.factory.authentication.OnMemoryCredential.Credentials;

/**
 * @author Ngoc Huy
 *
 */
@Component(CredentialFactory.NAME)
@Primary
public class CredentialFactory implements ContextBuilder {

	public static final String NAME = "adn.application.context.CredentialFactory";

	public static final int ROLE_CREDENTIAL_POSITION = 0;
	public static final int DEPARTMENT_ID_CREDENTIAL_POSITION = 1;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(Credential.class));

		Set<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.ROOT_PACKAGE);
		Set<Set<? extends Credential>> credentialSets = new HashSet<>(0);

		try {
			for (BeanDefinition def : beanDefs) {
				Class<? extends Credential> credentialType = (Class<? extends Credential>) Class
						.forName(def.getBeanClassName());

				if (EnumeratedCredential.class.isAssignableFrom(credentialType)) {
					Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) credentialType;
					Set<Credential> enums = Stream.of(enumType.getEnumConstants()).map(enumVal -> (Credential) enumVal)
							.collect(Collectors.toSet());

					credentialSets.add(enums);
					logger.debug(String.format("Added\n\t%s\nto Credential list",
							enums.stream().map(val -> val.toString()).collect(Collectors.joining("\n\t"))));
					continue;
				}

				boolean shouldThrow = true;

				for (Field field : credentialType.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Credentials.class)) {
						Collection<Credential> credentials = (Collection<Credential>) field.get(null);

						credentialSets.add(new HashSet<>(credentials));
						logger.debug(String.format("\nAdded\n\t%s\nto Credential list",
								credentials.stream().map(val -> val.evaluate()).collect(Collectors.joining("\n\t"))));
						shouldThrow = false;
						break;
					}
				}

				if (!shouldThrow) {
					continue;
				}

				throw new IllegalArgumentException(String.format("Unable to find any credentials in %s of type %s",
						OnMemoryCredential.class.getName(), def.getBeanClassName()));
			}
		} catch (Exception any) {
			any.printStackTrace();
			return;
		}
		logger.info("Finished building " + this.getClass());
	}

}
