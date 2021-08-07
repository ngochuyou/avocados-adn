/**
 * 
 */
package adn.controller.query.filter;

import java.util.UUID;

/**
 * @author Ngoc Huy
 *
 */
public class UUIDFilter implements SingularValueFilter<UUID>, PluralValueFilter<UUID> {

	private UUID[] in;

	private UUID[] notIn;

	private UUID equals;

	private UUID ne;

	private String like;

	private UUID is;

	private UUID isNot;

	@Override
	public UUID[] getIn() {
		return in;
	}

	@Override
	public UUID[] getNotIn() {
		return notIn;
	}

	@Override
	public UUID getEquals() {
		return equals;
	}

	@Override
	public UUID getNe() {
		return ne;
	}

	@Override
	public String getLike() {
		return like.toString();
	}

	@Override
	public UUID getIs() {
		return is;
	}

	@Override
	public UUID getIsNot() {
		return isNot;
	}

	public void setIn(UUID[] in) {
		this.in = in;
	}

	public void setNotIn(UUID[] notIn) {
		this.notIn = notIn;
	}

	public void setEquals(UUID equals) {
		this.equals = equals;
	}

	public void setNe(UUID ne) {
		this.ne = ne;
	}

	public void setLike(String like) {
		this.like = like;
	}

	public void setIs(UUID is) {
		this.is = is;
	}

	public void setIsNot(UUID isNot) {
		this.isNot = isNot;
	}

}
