/**
 * 
 */
package adn.service.resource.event;

import adn.service.resource.local.ResourceEntry;

/**
 * @author Ngoc Huy
 *
 */
public class PersistEventExecutor implements EventExecutor<PersistentEvent<?>> {

	public static PersistEventExecutor INSTANCE = new PersistEventExecutor();

	private PersistEventExecutor() {}

	@Override
	public void execute(PersistentEvent<?> event) {
		// TODO Auto-generated method stub
		ResourceEntry<?> entry = event.getResourceManager().getResourceContext().getEntry(event.getInstance());
	}

}
