/**
 * 
 */
package adn.controller.query.filter;

/**
 * @author Ngoc Huy
 *
 */
public abstract class AbstractSingularValueFilter<T> implements SingularValueFilter<T> {

	private T equals;

	private T ne;

	private String like;

	private T is;

	private T isNot;

	private T from;

	private T to;

	@Override
	public T getEquals() {
		return equals;
	}

	@Override
	public T getNe() {
		return ne;
	}

	@Override
	public String getLike() {
		return "%" + like + "%";
	}

	@Override
	public T getIs() {
		return is;
	}

	@Override
	public T getIsNot() {
		return isNot;
	}

	@Override
	public T getFrom() {
		return from;
	}

	@Override
	public T getTo() {
		return to;
	}

	public void setEquals(T equals) {
		this.equals = equals;
	}

	public void setNe(T ne) {
		this.ne = ne;
	}

	public void setLike(String like) {
		this.like = like;
	}

	public void setIs(T is) {
		this.is = is;
	}

	public void setIsNot(T isNot) {
		this.isNot = isNot;
	}

	public void setFrom(T from) {
		this.from = from;
	}

	public void setTo(T to) {
		this.to = to;
	}

}
