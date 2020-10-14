/**
 * 
 */
package adn.service.context;

/**
 * @author Ngoc Huy
 *
 */
public interface PersisterChain extends ResourcePersister {

	void register(ResourcePersister persister);

}
