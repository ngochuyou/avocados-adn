/**
 * 
 */
package adn.service.resource.engine.access;

import java.util.Optional;

import org.hibernate.property.access.spi.Getter;
import org.hibernate.property.access.spi.PropertyAccessStrategy;
import org.hibernate.property.access.spi.Setter;

import adn.service.resource.engine.access.PropertyAccessStrategyFactory.PropertyAccessBuilder;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractPropertyAccess implements PropertyAccessBuilder {

	protected Getter getter;
	protected Setter setter;
	protected PropertyAccessStrategy strategy = PropertyAccessStrategyFactory.DELEGATE_ACCESS_STRATEGY;

	AbstractPropertyAccess() {}

	AbstractPropertyAccess(Getter getter, Setter setter) {
		this.getter = getGetterOrNoOp(getter);
		this.setter = getSetterOrNoOp(setter);
	}

	@Override
	public PropertyAccessStrategy getPropertyAccessStrategy() {
		return strategy;
	}

	@Override
	public PropertyAccessBuilder setPropertyAccessStrategy(PropertyAccessStrategy strategy) {
		this.strategy = strategy == null ? PropertyAccessStrategyFactory.DELEGATE_ACCESS_STRATEGY : strategy;
		return this;
	}

	@Override
	public PropertyAccessBuilder setGetter(Getter getter) {
		this.getter = getGetterOrNoOp(getter);
		return this;
	}

	@Override
	public PropertyAccessBuilder setSetter(Setter setter) {
		this.setter = getSetterOrNoOp(setter);
		return this;
	}

	@Override
	public Getter getGetter() {
		return getter;
	}

	@Override
	public Setter getSetter() {
		return setter;
	}

	private Getter getGetterOrNoOp(Getter getter) {
		return Optional.ofNullable(getter).orElse(NoAccess.NO_OP_GETTER);
	}

	private Setter getSetterOrNoOp(Setter setter) {
		return Optional.ofNullable(setter).orElse(NoAccess.NO_OP_SETTER);
	}

	@Override
	public String toString() {
		return String.format("%s(getter=[%s], setter=[%s])", this.getClass().getSimpleName(), getter.toString(),
				setter.toString());
	}

}
