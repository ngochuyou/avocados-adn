/**
 * 
 */
package adn.controller.query.filter;

public class StringFilter implements SingularValueFilter<String>, PluralValueFilter<String> {

	private String equals;

	private String ne;

	private String like;

	private String is;

	private String isNot;

	private String[] in;

	private String[] notIn;

	@Override
	public String getEquals() {
		return equals;
	}

	@Override
	public String getNe() {
		return ne;
	}

	@Override
	public String getLike() {
		return String.format("%%%s%%", like);
	}

	@Override
	public String getIs() {
		return is;
	}

	@Override
	public String getIsNot() {
		return isNot;
	}

	@Override
	public String[] getIn() {
		return in;
	}

	@Override
	public String[] getNotIn() {
		return notIn;
	}

	public void setEquals(String equals) {
		this.equals = equals;
	}

	public void setNe(String ne) {
		this.ne = ne;
	}

	public void setLike(String like) {
		this.like = like;
	}

	public void setIs(String is) {
		this.is = is;
	}

	public void setIsNot(String isNot) {
		this.isNot = isNot;
	}

	public void setIn(String[] in) {
		this.in = in;
	}

	public void setNotIn(String[] notIn) {
		this.notIn = notIn;
	}

}