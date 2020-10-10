package adn.service.builder;

public abstract class TransactionComponent {

	protected ServiceTransaction parent;

	public TransactionComponent(ServiceTransaction parent) {
		super();
		this.parent = parent;
	}

	public ServiceTransaction and() {
		
		return this.parent;
	}

}
