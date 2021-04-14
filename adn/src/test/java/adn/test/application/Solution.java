/**
 * 
 */
package adn.test.application;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ngoc Huy
 *
 */
public class Solution {

	/**
	 * @param args
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static void main(String[] args) throws NoSuchMethodException, SecurityException {
		// TODO Auto-generated method stub
		int[] arr = new int[] { 6, 5, 10, 9, 8, 7, 7, 3, 1 };
		int sum = 10;

		for (int i : find(arr, sum)) {
			System.out.println(i);
		}
	}

	public static int[] find(int[] arr, int sum) {
		Map<Integer, Integer> pairs = new HashMap<>();
		Integer t;

		for (int i : arr) {
			if ((t = pairs.get(sum - i)) != null) {
				return new int[] { sum - i, t };
			}

			pairs.put(sum - i, i);
		}

		return null;
	}

}
