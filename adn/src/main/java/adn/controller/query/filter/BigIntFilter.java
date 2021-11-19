/**
 * 
 */
package adn.controller.query.filter;

import java.math.BigInteger;

/**
 * @author Ngoc Huy
 *
 */
public class BigIntFilter extends AbstractSingularValueFilter<BigInteger> implements PluralValueFilter<BigInteger> {

	@Override
	public BigInteger[] getIn() {
		return null;
	}

	@Override
	public BigInteger[] getNotIn() {
		return null;
	}

}
