/**
 * 
 */
package adn.model.factory.authentication.dynamicmap;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import adn.model.factory.authentication.Credential;

/**
 * @author Ngoc Huy
 *
 */
public class PartionalCredential implements Credential {

	private final Set<Credential> credentials;
	private final int position;
	private final String evaluation;

	public PartionalCredential(int position, Credential... credentials) {
		super();
		this.credentials = Set.of(credentials);
		this.position = position;
		this.evaluation = Stream.of(credentials).map(credential -> credential.evaluate())
				.collect(Collectors.joining(Credential.DEFAULT_DELIMITER));
	}

	@Override
	public String evaluate() {
		return evaluation;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public boolean contains(Credential credential) {
		return credentials.contains(credential);
	}

}
