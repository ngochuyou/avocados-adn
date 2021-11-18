/**
 * 
 */
package adn.controller.query.filter;

import java.util.UUID;

/**
 * @author Ngoc Huy
 *
 */
public class UUIDFilter extends AbstractSingularValueFilter<UUID> implements PluralValueFilter<UUID> {

	private UUID[] in;

	private UUID[] notIn;

	@Override
	public UUID[] getIn() {
		return in;
	}

	@Override
	public UUID[] getNotIn() {
		return notIn;
	}

	public void setIn(UUID[] in) {
		this.in = in;
	}

	public void setNotIn(UUID[] notIn) {
		this.notIn = notIn;
	}

}
