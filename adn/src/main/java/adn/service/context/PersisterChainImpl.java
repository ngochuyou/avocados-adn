/**
 * 
 */
package adn.service.context;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * A chained of one or multiple persister that might support persisting
 * procedure of some persistence
 * 
 * @author Ngoc Huy
 *
 */
public class PersisterChainImpl implements PersisterChain {

	private PersisterChainImpl chain;

	private ResourcePersister persister;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public PersisterChainImpl(PersisterChainImpl chain, ResourcePersister persister) {
		super();
		this.chain = chain;
		Assert.notNull(persister, "persister cannot be null");
		this.persister = persister;
	}

	@Override
	public Resource persist(Object o) throws PersistenceException {
		// TODO Auto-generated method stub
		if (this.persister.supports(o)) {
			logger.debug("One match found: " + this.persister.getClass());
			return this.persister.persist(o);
		}

		if (this.chain == null) {
			throw new PersistenceException("PersisterChain doesn't support instance of: " + o.getClass());
		}

		return this.chain.persist(o);
	}

	@Override
	public boolean supports(Object o) {
		// TODO Auto-generated method stub
		if (this.persister.supports(o)) {
			return true;
		}

		return this.chain == null ? false : this.chain.supports(o);
	}

	@Override
	public void register(ResourcePersister persister) {
		// TODO Auto-generated method stub
		if (this.chain == null) {
			this.chain = new PersisterChainImpl(null, persister);
			
			return;
		}

		this.chain.register(persister);
	}

}
