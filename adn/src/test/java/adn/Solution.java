/**
 * 
 */
package adn;

import adn.utilities.Strings;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		long t = System.currentTimeMillis();
		
		String s = "pathname";
		
		System.out.println(Strings.toCamel("get" + s, " "));
		
		System.out.println(System.currentTimeMillis() - t);
	}

}
