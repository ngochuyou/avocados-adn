/**
 * 
 */
package adn.application.context.builders;

import static adn.service.DepartmentCredential.CREDENTIALS;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
import adn.service.DepartmentCredential;
import adn.service.internal.Role;

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
	public static final int CREDENTIAL_COMPONENT_POSITION = 1;

	private List<Credential> credentials;

	@SuppressWarnings("unchecked")
	@Override
	public void buildAfterStartUp() throws Exception {
		Logger logger = LoggerFactory.getLogger(this.getClass());

		logger.info("Building " + this.getClass());

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

		scanner.addIncludeFilter(new AssignableTypeFilter(Credential.class));

		Collection<BeanDefinition> beanDefs = scanner.findCandidateComponents(Constants.ROOT_PACKAGE);
		Collection<Collection<Credential>> credentialsSets = new HashSet<>(0);
		boolean shouldThrow;

		for (BeanDefinition def : beanDefs) {
			Class<? extends Credential> credentialType = (Class<? extends Credential>) Class
					.forName(def.getBeanClassName());

			if (EnumeratedCredential.class.isAssignableFrom(credentialType)) {
				Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) credentialType;
				Set<Credential> enums = Stream.of(enumType.getEnumConstants()).map(enumVal -> (Credential) enumVal)
						.collect(Collectors.toSet());

				if (enums.isEmpty()) {
					throw new IllegalArgumentException(String.format("Unable to find any credentials in %s of type %s",
							EnumeratedCredential.class.getName(), enumType.getName()));
				}

				credentialsSets.add(enums);
				logger.debug(String.format("\nAdded\n\t%s\nto Credential list",
						enums.stream().map(val -> val.toString()).collect(Collectors.joining("\n\t"))));
				continue;
			}

			if (OnMemoryCredential.class.isAssignableFrom(credentialType)) {
				shouldThrow = true;

				for (Field field : credentialType.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Credentials.class)) {
						Collection<Credential> credentials = Map.class.isAssignableFrom(field.getType())
								? ((Map<?, Credential>) field.get(null)).values()
								: (Collection<Credential>) field.get(null);

						credentialsSets.add(new HashSet<>(credentials));
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
		}

		credentials = Collections.unmodifiableList(resolveCredentialComponents(credentialsSets));

		logger.debug(String.format("Found following Credentials\n\t%s",
				credentials.stream().map(credential -> credential.evaluate()).collect(Collectors.joining("\n\t"))));

		logger.info("Finished building " + this.getClass());
	}

	private List<Credential> resolveCredentialComponents(Collection<Collection<Credential>> credentialsSets) {
		List<Credential> finalCredentials = new ArrayList<>();
		// individually add credentials
		finalCredentials.addAll(credentialsSets.stream().flatMap(set -> set.stream()).collect(Collectors.toList()));
		// set emptiness must be asserted as a contract
		List<List<Credential>> sortedCredentialsCollection = credentialsSets.stream().map(set -> new ArrayList<>(set))
				.sorted((setA, setB) -> Integer.compare(setA.get(0).getPosition(), setB.get(0).getPosition()))
				.collect(Collectors.toList());

		int collectionSize = sortedCredentialsCollection.size();
		List<Credential> currentCredentials;
		List<List<Credential>> subCredentials;
		int n;
		// start building compound credential starting from a credential
		// combined from 2 other Credentials, up to n (collectionSize)
		// ex:
		// Credential collections A: "A", "B"
		// Credential collections B: "1"
		// Credential collections C: "true"
		//
		// then we have credentials like A::1, B::1, A::1::true, B::1::true
		for (int compoundSpan = 2; compoundSpan <= collectionSize; compoundSpan++) {
			for (int rowIndex = 0; rowIndex + compoundSpan <= collectionSize; rowIndex++) {
				currentCredentials = sortedCredentialsCollection.get(rowIndex);
				subCredentials = sortedCredentialsCollection.subList(rowIndex + 1, rowIndex + compoundSpan);
				n = currentCredentials.size();

				for (int colIndex = 0; colIndex < n; colIndex++) {
					finalCredentials.addAll(multiDistribute(CREDENTIAL_COMPONENT_POSITION,
							currentCredentials.get(colIndex), subCredentials, 0));
				}
			}
		}

		return finalCredentials;
	}

	private List<Credential> multiDistribute(int position, Credential credential, List<List<Credential>> targets,
			int index) {
		if (index == targets.size() - 1) {
			return distribute(position, credential, targets.get(index));
		}
		// @formatter:off
		return distribute(position, credential, targets.get(index)).stream()
				.map(result -> multiDistribute(position, result, targets, index + 1))
				.flatMap(list -> list.stream())
				.collect(Collectors.toList());
		// @formatter:on
	}

	private List<Credential> distribute(int position, Credential distribution, List<Credential> target) {
		return target.stream().map(credential -> new CompoundCredential(position, distribution, credential))
				.collect(Collectors.toList());
	}

	public static class CompoundCredential implements Credential {

		private static final String DELIMITER = "::";

		private final int position;
		private final Credential credential;
		private final CompoundCredential nextCredential;

		protected CompoundCredential(int position, Credential... credentials) {
			super();
			this.position = position;

			if (credentials == null || credentials.length == 0) {
				throw new IllegalArgumentException("Credentials was empty");
			}

			credential = Objects.requireNonNull(credentials[0], "Credential was null");

			if (credentials.length == 1) {
				nextCredential = null;
				return;
			}

			Credential[] otherCredentials = Arrays.copyOfRange(credentials, 1, credentials.length);

			nextCredential = new CompoundCredential(position, otherCredentials);
		}

		public CompoundCredential(Credential credential, CompoundCredential nextCredential, int position) {
			super();
			this.credential = credential;
			this.nextCredential = nextCredential;
			this.position = position;
		}

		@Override
		public String evaluate() {
			if (nextCredential == null) {
				return credential.evaluate();
			}

			return credential.evaluate() + DELIMITER + nextCredential.evaluate();
		}

		@Override
		public int getPosition() {
			return position;
		}

		public Credential getCredential() {
			return credential;
		}

		public CompoundCredential getNextCredential() {
			return nextCredential;
		}

	}

	/**
	 * @return the credentials
	 */
	public List<Credential> getCredentials() {
		return Collections.unmodifiableList(credentials);
	}

	private static DepartmentCredential resolveDepartmentCredential(UUID departmentId) {
		DepartmentCredential credential = CREDENTIALS.get(departmentId);

		return credential != null ? credential : CREDENTIALS.get(DepartmentScopeContext.unknown());
	}

	public static Credential from(Role role, UUID departmentId) {
		if (role == null && departmentId == null) {
			throw new IllegalArgumentException("%s and department ID cannot be both null");
		}

		if (role != null && departmentId != null) {
			return new CompoundCredential(CREDENTIAL_COMPONENT_POSITION, role,
					resolveDepartmentCredential(departmentId));
		}

		if (role != null) {
			return role;
		}

		return resolveDepartmentCredential(departmentId);
	}

}
