/**
 * 
 */
package adn.model.factory.authentication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;

/**
 * Credentials that are usually initialised/hardcoded, such as static field
 * fetched from the database.
 * 
 * Implementations of this interface have to have a public static
 * {@link Collection} of type {@link Credential} including every Credential
 * instances of its type, annotated with {@link OnMemoryCredential.Credentials}
 * 
 * @author Ngoc Huy
 *
 */
public interface OnMemoryCredential extends Credential {

	/**
	 * Marker for {@link OnMemoryCredential} instances
	 * 
	 * @author Ngoc Huy
	 *
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Credentials {
	}

}
