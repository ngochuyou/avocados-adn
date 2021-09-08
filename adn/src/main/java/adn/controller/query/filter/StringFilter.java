/**
 * 
 */
package adn.controller.query.filter;

public class StringFilter extends AbstractSingularValueFilter<String>
		implements SingularValueFilter<String>, PluralValueFilter<String> {

	private String[] in;

	private String[] notIn;

	@Override
	public String[] getIn() {
		return in;
	}

	@Override
	public String[] getNotIn() {
		return notIn;
	}

	public void setIn(String[] in) {
		this.in = in;
	}

	public void setNotIn(String[] notIn) {
		this.notIn = notIn;
	}

}